package io.github.copperlight.skeleton

object SystemClock extends Clock {

  /** Current wall time in milliseconds since the epoch. Typically equivalent to
    * System.currentTimeMillis.
    */
  override def wallTime: Long = System.currentTimeMillis

  /** Current time from a monotonic clock source. The value is only meaningful when compared with
    * another snapshot to determine the elapsed time for an operation. The difference between two
    * samples will have a unit of nanoseconds. The returned value is typically equivalent to
    * System.nanoTime.
    */
  override def monotonicTime: Long = System.nanoTime
}
