package ai.powerstats.common
package model

case class Account(id: Long,
                   email: String,
                   password_hash: Array[Byte])
