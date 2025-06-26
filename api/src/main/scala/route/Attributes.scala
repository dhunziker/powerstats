package dev.powerstats.api
package route

import sttp.tapir.AttributeKey

object Attributes {
  val rateLimit = new AttributeKey[Int]("rateLimit")
}
