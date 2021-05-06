package io.github.copperlight.skeleton

import munit.FunSuite

import java.io.IOError
import java.time.Duration
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

class SchedulerSuite extends FunSuite {
  test("update next FIXED_DELAY") {
    val clock = new ManualClock
    val stats = Scheduler.Stats

    val options = Scheduler.Options.withFrequency(
      Scheduler.Policy.FIXED_DELAY,
      Duration.ofSeconds(10)
    )

    clock.setWallTime(5437L)
    val task = new Scheduler.DelayedTask(clock, options, () => {})
    assertEquals(5437L, task.getNextExecutionTime)
    assertEquals(0, stats.skipped.get)

    clock.setWallTime(12123L)
    task.updateNextExecutionTime(stats)
    assertEquals(22123L, task.getNextExecutionTime)
    assertEquals(0, stats.skipped.get)

    clock.setWallTime(27000L)
    task.updateNextExecutionTime(stats)
    assertEquals(37000L, task.getNextExecutionTime)
    assertEquals(0, stats.skipped.get)
  }

  test("update next FIXED_RATE skip") {
    val clock = new ManualClock
    val stats = Scheduler.Stats

    val options = Scheduler.Options.withFrequency(
      Scheduler.Policy.FIXED_RATE_SKIP_IF_LONG,
      Duration.ofSeconds(10)
    )

    clock.setWallTime(5437L)
    val task = new Scheduler.DelayedTask(clock, options, () => {})
    assertEquals(5437L, task.getNextExecutionTime)
    assertEquals(0, stats.skipped.get)

    clock.setWallTime(12123L)
    task.updateNextExecutionTime(stats)
    assertEquals(15437L, task.getNextExecutionTime)
    assertEquals(0, stats.skipped.get)

    clock.setWallTime(27000L)
    task.updateNextExecutionTime(stats)
    assertEquals(35437L, task.getNextExecutionTime)
    assertEquals(1, stats.skipped.get)

    clock.setWallTime(57000L)
    task.updateNextExecutionTime(stats)
    assertEquals(65437L, task.getNextExecutionTime)
    assertEquals(3, stats.skipped.get)
  }

  test("shutdown stops threads") {
    def numberOfThreads(id: String): Long = {
      Thread.getAllStackTraces.keySet.stream
        .filter((t: Thread) => t.getName.startsWith("scheduler-" + id))
        .count
    }

    val s = new Scheduler(SystemClock, "shutdown", 1)

    val opts = Scheduler.Options
      .withFrequency(
        Scheduler.Policy.FIXED_RATE_SKIP_IF_LONG,
        Duration.ofMillis(10)
      )
      .withStopOnFailure(false)

    // schedule something, to start the thread
    s.schedule(opts, () => {})
    assertEquals(1L, numberOfThreads("shutdown"))

    // shutdown and wait, to give the thread a chance to restart
    s.shutdownThreads()
    Thread.sleep(300)
    assertEquals(0L, numberOfThreads("shutdown"))
  }

  test("stopOnFailure=false throwable") {
    val s = new Scheduler(SystemClock, "test", 1)

    val opts = Scheduler.Options
      .withFrequency(
        Scheduler.Policy.FIXED_RATE_SKIP_IF_LONG,
        Duration.ofMillis(10)
      )
      .withStopOnFailure(false)

    val latch = new CountDownLatch(5)

    val f = s.schedule(
      opts,
      () => {
        latch.countDown()
        throw new IOError(new RuntimeException("stop"))
      }
    )

    assert(latch.await(60, TimeUnit.SECONDS))
    assert(!f.isDone)
    s.shutdownThreads()
  }

  test("stopOnFailure=false") {
    val s = new Scheduler(SystemClock, "test", 2)

    val opts = Scheduler.Options
      .withFrequency(
        Scheduler.Policy.FIXED_DELAY,
        Duration.ofMillis(10)
      )
      .withStopOnFailure(false)

    val latch = new CountDownLatch(5)

    val f = s.schedule(
      opts,
      () => {
        latch.countDown()
        throw new RuntimeException("stop")
      }
    )

    assert(latch.await(60, TimeUnit.SECONDS))
    assert(!f.isDone)
    s.shutdownThreads()
  }

  test("stopOnFailure=true") {
    val s = new Scheduler(SystemClock, "test", 2)

    val opts = Scheduler.Options
      .withFrequency(
        Scheduler.Policy.FIXED_DELAY,
        Duration.ofMillis(10)
      )
      .withStopOnFailure(true)

    val latch = new CountDownLatch(1)

    val f = s.schedule(
      opts,
      () => {
        latch.countDown()
        throw new RuntimeException("stop")
      }
    )

    assert(latch.await(60, TimeUnit.SECONDS))
    // wait for isDone to propagate. this will be endless, if the test breaks.
    while (!f.isDone) {}
    s.shutdownThreads()
  }

  test("cancel") {
    val s = new Scheduler(SystemClock, "test", 2)

    val opts = Scheduler.Options
      .withFrequency(
        Scheduler.Policy.FIXED_DELAY,
        Duration.ofMillis(10)
      )
      .withStopOnFailure(false)

    val latch = new CountDownLatch(1)
    val ref = new AtomicReference[ScheduledFuture[_]]

    ref.set(
      s.schedule(
        opts,
        () => {
          try {
            while (ref.get == null) {}
            ref.get.cancel(true)
            Thread.sleep(600000L)
          } catch {
            case _: InterruptedException =>
              latch.countDown()
          }
        }
      )
    )

    assert(latch.await(60, TimeUnit.SECONDS))
    assert(ref.get.isDone)
    s.shutdownThreads()
  }

  test("threads are replaced") {
    val s = new Scheduler(SystemClock, "test", 1)

    val opts = Scheduler.Options
      .withFrequency(
        Scheduler.Policy.FIXED_DELAY,
        Duration.ofMillis(10)
      )
      .withStopOnFailure(false)

    val latch = new CountDownLatch(10)

    s.schedule(
      opts,
      () => {
        latch.countDown()
        Thread.currentThread.interrupt()
      }
    )

    assert(latch.await(60, TimeUnit.SECONDS))
    s.shutdownThreads()
  }
}
