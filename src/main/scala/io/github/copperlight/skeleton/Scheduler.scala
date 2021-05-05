package io.github.copperlight.skeleton

import com.typesafe.scalalogging.StrictLogging

import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.DelayQueue
import java.util.concurrent.Delayed
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import scala.util.control.Breaks.break

/** Simple scheduler for recurring tasks based on a fixed size thread pool. This class is intended
  * for running short lived tasks at a regular interval.
  *
  * Usage
  *
  * val scheduler: Scheduler = new Scheduler("poller", 2)
  *
  * val options: Scheduler.Options = new Scheduler.Options().withFrequency(
  * Scheduler.Policy.FIXED_RATE_SKIP_IF_LONG, Duration.ofSeconds(10));
  *
  * scheduler.schedule(options, () => doWork())
  *
  * @param id
  *   Id for this instance of the scheduler. Used to distinguish between instances of the scheduler
  *   for thread names. Threads will be named "scheduler-$id-$i".
  * @param poolSize
  *   Number of threads in the pool. The threads will not be started until the first task is
  *   scheduled.
  */
class Scheduler(clock: Clock, id: String, poolSize: Int) extends StrictLogging {

  private val stats = Stats
  private val queue = new DelayQueue[DelayedTask]
  private val factory = newThreadFactory(id)
  private val threads = new Array[Thread](poolSize)
  private var started = false
  private var shutdown = false

  private def newThreadFactory(id: String): ThreadFactory = {
    new ThreadFactory() {
      private val next = new AtomicInteger

      override def newThread(r: Runnable): Thread = {
        val name = s"scheduler-$id-${next.getAndIncrement}"
        val t = new Thread(r, name)
        t.setDaemon(true)
        t
      }
    }
  }

  /** Schedule a repetitive task.
    *
    * @param options
    *   Options for controlling the execution of the task. See [[Options]] for more information.
    * @param task
    *   Task to execute.
    * @return
    *   Future that can be used for cancelling the current and future executions of the task. There
    *   is no value associated with the task so the future is just for checking if it is still
    *   running to stopping it from running in the future.
    */
  def schedule(options: Options.type, task: Runnable): ScheduledFuture[_] = {
    if (!started) startThreads()
    val t = new DelayedTask(clock, options, task)
    queue.put(t)
    t
  }

  /** Shutdown and cleanup resources associated with the scheduler. All threads will be interrupted,
    * but this method does not block for them to all finish execution.
    */
  def shutdownThreads(): Unit = {
    shutdown = true
    for (i <- threads.indices) {
      if (threads(i) != null && threads(i).isAlive) {
        threads(i).interrupt()
        threads(i) = null
      }
    }
  }

  private def startThreads(): Unit = {
    if (!shutdown) {
      started = true
      for (i <- threads.indices) {
        if (threads(i) == null || !threads(i).isAlive || threads(i).isInterrupted) {
          threads(i) = factory.newThread(new Worker)
          threads(i).start()
          logger.debug(s"started thread ${threads(i).getName}")
        }
      }
    }
  }

  /** Repetition policy for scheduled tasks.
    *
    *   - RUN_ONCE. Run a task once.
    *   - FIXED_DELAY. Run a task repeatedly, using a fixed delay between executions.
    *   - FIXED_RATE_SKIP_IF_LONG. Run a task repeatedly, attempting to maintain a consistent rate
    *     of execution. If the execution time is less than the desired frequency, then the start
    *     times will be at a consistent interval. If the execution time exceeds the frequency, then
    *     some executions will be skipped.
    *
    * The primary use case for FIXED_RATE_SKIP_IF_LONG is when we want to maintain a consistent
    * frequency, but want to avoid queuing up many tasks if the system cannot keep up. Fixed delay
    * is often inappropriate because for the normal case it will drift by the execution time of the
    * task.
    */
  object Policy extends Enumeration {
    val RUN_ONCE, FIXED_DELAY, FIXED_RATE_SKIP_IF_LONG = Value
  }

  /** Options to control how a task will get executed. */
  object Options {
    private[Scheduler] var schedulingPolicy = Policy.RUN_ONCE
    private[Scheduler] var initialDelay = 0L
    private[Scheduler] var frequencyMillis = 0L
    private[Scheduler] var stopOnFailure = false

    /** How long to wait after a task has been scheduled to the first execution. If not set, then it
      * will be scheduled immediately.
      */
    def withInitialDelay(delay: Duration): Options.type = {
      initialDelay = delay.toMillis
      this
    }

    /** Configure the task to execute repeatedly.
      *
      * @param policy
      *   Repetition schedulingPolicy to use for the task. See [[Policy]] for the supported options.
      * @param frequency
      *   How frequently to repeat the execution. The interpretation of this parameter will depend
      *   on the [[Policy]].
      */
    def withFrequency(policy: Policy.Value, frequency: Duration): Options.type = {
      schedulingPolicy = policy
      frequencyMillis = frequency.toMillis
      this
    }

    /** Should a repeated task stop executing if an exception propagates out of the task?
      *
      * Defaults to false.
      */
    def withStopOnFailure(flag: Boolean): Options.type = {
      this.stopOnFailure = flag
      this
    }
  }

  /** Collection of stats that are updated as part of executing the tasks. */
  object Stats {
    // TODO: add a thread which reports once per minute and resets values?

    /** Counter that tracks the number of active tasks. */
    private[Scheduler] val activeCount = new AtomicInteger()
    def incrementActiveTaskCount(): Unit = activeCount.incrementAndGet
    def decrementActiveTaskCount(): Unit = activeCount.decrementAndGet

    /** Timer for measuring the execution time of the task. */
    private[Scheduler] val taskExecutionTime = new AtomicLong()

    /** Timer for measuring the delay for the task. This should be close to zero, but if the system
      * is overloaded or having trouble, then there might be a large delay.
      */
    private[Scheduler] val taskExecutionDelay = new AtomicLong()

    /** Counter that will be incremented each time an expected execution is skipped when using
      * [[Policy#FIXED_RATE_SKIP_IF_LONG]].
      */
    private[Scheduler] val skipped = new AtomicInteger()
    def incrementSkipped(): Unit = skipped.incrementAndGet

    /** Counter for tracking the number of uncaught exceptions by the simple class name of the
      * exception.
      */
    private[Scheduler] val uncaughtExceptions = new ConcurrentHashMap[String, Int]()

    def incrementUncaught(t: Throwable): Unit = {
      val cls = t.getClass.getSimpleName
      uncaughtExceptions.putIfAbsent(cls, 1)
      uncaughtExceptions.computeIfPresent(cls, (_, count) => count + 1)
    }
  }

  /** Wraps the user supplied task with metadata for subsequent executions.
    *
    * @param clock
    *   Clock for computing the next execution time for the task.
    * @param options
    *   Options for how to repeat the execution.
    * @param task
    *   User specified task to execute.
    */
  class DelayedTask(
    val clock: Clock,
    val options: Options.type,
    val task: Runnable
  ) extends ScheduledFuture[Unit] {

    private val initialExecutionTime = clock.wallTime + options.initialDelay
    private var nextExecutionTime = initialExecutionTime

    @volatile
    private var thread = new Thread()

    @volatile
    private var cancelled = false

    /** Returns the next scheduled execution time. */
    def getNextExecutionTime: Long = nextExecutionTime

    /** Update the next execution time based on the options for this task. */
    private[Scheduler] def updateNextExecutionTime(stats: Stats.type): Unit = {
      options.schedulingPolicy match {
        case Policy.FIXED_DELAY =>
          nextExecutionTime = clock.wallTime + options.frequencyMillis

        case Policy.FIXED_RATE_SKIP_IF_LONG =>
          val now = clock.wallTime
          nextExecutionTime += options.frequencyMillis
          while (nextExecutionTime < now) {
            nextExecutionTime += options.frequencyMillis
            stats.incrementSkipped()
          }

        case _ =>
      }
    }

    /** Execute the task and if reschedule another execution.
      *
      * @param queue
      *   Queue for the pool. This task will be added to the queue to schedule future executions.
      */
    def runAndReschedule(
      queue: DelayQueue[DelayedTask],
      stats: Stats.type
    ): Unit = {
      thread = Thread.currentThread
      var scheduleAgain = options.schedulingPolicy ne Policy.RUN_ONCE
      try if (!isDone) {
        task.run()
      } catch {
        case t: Throwable =>
          // This catches Throwable because we cannot control the task and thus cannot
          // ensure it is well behaved with respect to exceptions.
          logger.warn("task execution failed", t)
          stats.incrementUncaught(t)
          scheduleAgain = !options.stopOnFailure
      } finally {
        thread = null
        if (scheduleAgain && !isDone) {
          updateNextExecutionTime(stats.skipped)
          queue.put(this)
        } else {
          cancelled = true
        }
      }
    }

    override def getDelay(unit: TimeUnit): Long = {
      val delayMillis = Math.max(nextExecutionTime - clock.wallTime, 0L)
      unit.convert(delayMillis, TimeUnit.MILLISECONDS)
    }

    override def compareTo(other: Delayed): Int = {
      val d1 = getDelay(TimeUnit.MILLISECONDS)
      val d2 = other.getDelay(TimeUnit.MILLISECONDS)
      d1.compare(d2)
    }

    override def cancel(mayInterruptIfRunning: Boolean): Boolean = {
      cancelled = true
      val t = thread
      if (mayInterruptIfRunning && t != null) {
        t.interrupt()
      }
      true
    }

    override def isCancelled: Boolean = {
      cancelled
    }

    override def isDone: Boolean = {
      cancelled
    }

    override def get: Unit = {
      throw new UnsupportedOperationException
    }

    override def get(timeout: Long, unit: TimeUnit): Unit = {
      throw new UnsupportedOperationException
    }
  }

  /** Actual task running in the threads. It will block on trying to get a task to execute from the
    * queue until a task is ready.
    */
  private final class Worker extends Runnable {

    override def run(): Unit = {
      try {
        // Note: do not use Thread.interrupted() because it will clear the interrupt
        // status of the thread.
        while (!Thread.currentThread.isInterrupted) {
          try {
            val task = queue.take
            stats.incrementActiveTaskCount()
            stats.taskExecutionDelay.set((clock.wallTime - task.getNextExecutionTime) / 1000)

            val start = clock.wallTime
            task.runAndReschedule(queue, stats)
            stats.taskExecutionTime.set((clock.wallTime - start) / 1000)
          } catch {
            case e: InterruptedException =>
              logger.debug("task interrupted", e)
              break
          } finally {
            stats.decrementActiveTaskCount()
          }
        }
      } finally {
        startThreads()
      }
    }
  }
}
