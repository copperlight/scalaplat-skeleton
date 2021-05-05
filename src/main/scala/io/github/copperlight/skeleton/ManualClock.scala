package io.github.copperlight.skeleton

import java.util.concurrent.atomic.AtomicLong

/** Clock implementation that allows the user to explicitly control the time. Typically used for
  * unit tests.
  *
  * @param wallInit
  *   Initial value for the wall time.
  * @param monotonicInit
  *   Initial value for the monotonic time.
  */
class ManualClock(wallInit: Long = 0L, monotonicInit: Long = 0L) extends Clock {

  private val wall = new AtomicLong(wallInit)
  private val monotonic = new AtomicLong(monotonicInit)

  override def wallTime: Long = wall.get
  override def monotonicTime: Long = monotonic.get

  def setWallTime(t: Long): Unit = wall.set(t)
  def setMonotonicTime(t: Long): Unit = monotonic.set(t)
}
