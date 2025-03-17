package ai.powerstats.common
package exception

case class EventCreateError(message: String) extends Error(message)
