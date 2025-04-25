package ai.powerstats.common
package config

import com.comcast.ip4s.*
import io.circe.Decoder

import scala.util.Try

case class Api(port: Port,
               host: Host,
               jwtKey: String,
               baseUrl: String)

object Api {
  implicit val decodePort: Decoder[Port] = Decoder.decodeInt.emapTry { v =>
    Try(Port.fromInt(v).getOrElse(port"8080"))
  }
  implicit val decodeHost: Decoder[Host] = Decoder.decodeString.emapTry { v =>
    Try(Host.fromString(v).getOrElse(host"0.0.0.0"))
  }
}
