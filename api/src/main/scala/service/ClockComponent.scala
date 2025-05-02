package dev.powerstats.api
package service

import java.time.Clock

trait ClockComponent {
  implicit def clock: Clock
}
