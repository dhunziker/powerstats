package dev.powerstats.api
package route.request

case class MeetSearchRequest(federation: Option[String],
                             meetCountry: Option[String],
                             meetName: Option[String])
