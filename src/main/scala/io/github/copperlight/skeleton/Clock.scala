package io.github.copperlight.skeleton

/** A timing source that can be used to access the current wall time as well as a high resolution
  * monotonic time to measuring elapsed times. Most of the time the [[Clock#SYSTEM]] implementation
  * that calls the builtin java methods is probably the right one to use. Other implementations
  * would typically only get used for unit tests or other cases where precise control of the clock
  * is needed.
  */
trait Clock {

  /** Current wall time in milliseconds since the epoch. Typically equivalent to
    * System.currentTimeMillis.
    */
  def wallTime: Long

  /** Current time from a monotonic clock source. The value is only meaningful when compared with
    * another snapshot to determine the elapsed time for an operation. The difference between two
    * samples will have a unit of nanoseconds. The returned value is typically equivalent to
    * System.nanoTime.
    */
  def monotonicTime: Long

  /** Default clock implementation based on corresponding calls in [[java.lang.System]].
    */
  val SYSTEM: Clock = SystemClock
}
