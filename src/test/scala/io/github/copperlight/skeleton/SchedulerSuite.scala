package io.github.copperlight.skeleton

import munit.FunSuite

import java.time.Duration
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

class SchedulerSuite extends FunSuite {
  test("updateNextFixedDelay") {
    val clock = new ManualClock
//    val registry = new DefaultRegistry(clock)
//    val skipped = registry.counter("skipped")
    val options =
      new Scheduler.Options.withFrequency(Scheduler.Policy.FIXED_DELAY, Duration.ofSeconds(10))
    clock.setWallTime(5437L)
    val task = new Scheduler.DelayedTask(
      clock,
      options,
      () => {
        def foo(): Unit = {}
        foo()
      }
    )
    assertEquals(5437L, task.getNextExecutionTime)
    assertEquals(0L, skipped.count)
    clock.setWallTime(12123L)
    task.updateNextExecutionTime(skipped)
    assertEquals(22123L, task.getNextExecutionTime)
    assertEquals(0L, skipped.count)
    clock.setWallTime(27000L)
    task.updateNextExecutionTime(skipped)
    assertEquals(37000L, task.getNextExecutionTime)
    assertEquals(0L, skipped.count)
  }

  test("updateNextFixedRateSkip") {
    val clock = new ManualClock
    val registry = new DefaultRegistry(clock)
    val skipped = registry.counter("skipped")
    val options = new Scheduler.Options()
      .withFrequency(Scheduler.Policy.FIXED_RATE_SKIP_IF_LONG, Duration.ofSeconds(10))
    clock.setWallTime(5437L)
    val task = new Scheduler.DelayedTask(
      clock,
      options,
      () => {
        def foo(): Unit = {}
        foo()
      }
    )
    assertEquals(5437L, task.getNextExecutionTime)
      .assertEquals(0L, skipped.count)
    clock.setWallTime(12123L)
    task.updateNextExecutionTime(skipped)
    assertEquals(15437L, task.getNextExecutionTime)
    assertEquals(0L, skipped.count)
    clock.setWallTime(27000L)
    task.updateNextExecutionTime(skipped)
    assertEquals(35437L, task.getNextExecutionTime)
    assertEquals(1L, skipped.count)
    clock.setWallTime(57000L)
    task.updateNextExecutionTime(skipped)
    assertEquals(65437L, task.getNextExecutionTime)
    assertEquals(3L, skipped.count)
  }

  private def numberOfThreads(id: String): Long = {
    Thread.getAllStackTraces.keySet.stream
      .filter((t: Thread) => t.getName.startsWith("spectator-" + id))
      .count
  }

  test("shutdownStopsThreads") {
    val s = new Scheduler(new DefaultRegistry, "shutdown", 1)
    // Schedule something to force it to start the threads
    val opts = new Scheduler.Options()
      .withFrequency(Scheduler.Policy.FIXED_RATE_SKIP_IF_LONG, Duration.ofMillis(10))
      .withStopOnFailure(false)
    val f = s.schedule(
      opts,
      () => {
        def foo(): Unit = {}

        foo()
      }
    )
    assertEquals(1L, numberOfThreads("shutdown"))
    // Shutdown and wait a bit, this gives the thread a chance to restart
    s.shutdown()
    Thread.sleep(300)
    assertEquals(0L, numberOfThreads("shutdown"))
  }

  test("stopOnFailureFalseThrowable") {
    val s = new Scheduler(new DefaultRegistry, "test", 1)
    val opts = new Scheduler.Options()
      .withFrequency(Scheduler.Policy.FIXED_RATE_SKIP_IF_LONG, Duration.ofMillis(10))
      .withStopOnFailure(false)
    val latch = new CountDownLatch(5)
    val f = s.schedule(
      opts,
      () => {
        def foo() = {
          latch.countDown()
          throw new IOError(new RuntimeException("stop"))
        }

        foo()
      }
    )
    assert(latch.await(60, TimeUnit.SECONDS))
    assert(!f.isDone)
    s.shutdown()
  }

  test("stopOnFailureFalse") {
    val s = new Scheduler(new DefaultRegistry, "test", 2)
    val opts = new Scheduler.Options()
      .withFrequency(Scheduler.Policy.FIXED_DELAY, Duration.ofMillis(10))
      .withStopOnFailure(false)
    val latch = new CountDownLatch(5)
    val f = s.schedule(
      opts,
      () => {
        def foo() = {
          latch.countDown()
          throw new RuntimeException("stop")
        }

        foo()
      }
    )
    assert(latch.await(60, TimeUnit.SECONDS))
    assert(!f.isDone)
    s.shutdown()
  }

  test("stopOnFailureTrue") {
    val s = new Scheduler(new DefaultRegistry, "test", 2)
    val opts = new Scheduler.Options()
      .withFrequency(Scheduler.Policy.FIXED_DELAY, Duration.ofMillis(10))
      .withStopOnFailure(true)
    val latch = new CountDownLatch(1)
    val f = s.schedule(
      opts,
      () => {
        def foo() = {
          latch.countDown()
          throw new RuntimeException("stop")
        }

        foo()
      }
    )
    assert(latch.await(60, TimeUnit.SECONDS))
    while ({
      !f.isDone
    }) { // This will be an endless loop if broken
    }
    s.shutdown()
  }

  test("cancel") {
    val s = new Scheduler(new DefaultRegistry, "test", 2)
    val opts = new Scheduler.Options()
      .withFrequency(Scheduler.Policy.FIXED_DELAY, Duration.ofMillis(10))
      .withStopOnFailure(false)
    val latch = new CountDownLatch(1)
    val ref = new AtomicReference[ScheduledFuture[_]]
    ref.set(
      s.schedule(
        opts,
        () => {
          def foo() = try {
            while ({
              ref.get == null
            }) {}
            ref.get.cancel(true)
            Thread.sleep(600000L)
          } catch {
            case e: InterruptedException =>
              latch.countDown()
          }

          foo()
        }
      )
    )
    assert(latch.await(60, TimeUnit.SECONDS))
    assert(ref.get.isDone)
    s.shutdown()
  }

  test("threadsAreReplaced") {
    val s = new Scheduler("test", 1)
    val opts = new Scheduler.Options()
      .withFrequency(Scheduler.Policy.FIXED_DELAY, Duration.ofMillis(10))
      .withStopOnFailure(false)
    val latch = new CountDownLatch(10)
    s.schedule(
      opts,
      () => {
        def foo() = {
          latch.countDown()
          Thread.currentThread.interrupt()
        }

        foo()
      }
    )
    assert(latch.await(60, TimeUnit.SECONDS))
    s.shutdown()
  }
}
