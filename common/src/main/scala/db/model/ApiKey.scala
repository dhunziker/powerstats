package dev.powerstats.common
package db.model

import java.time.LocalDateTime

case class ApiKey(id: Long,
                  accountId: Long,
                  name: String,
                  publicKey: String,
                  secretKeyHash: Array[Byte],
                  creationDate: LocalDateTime,
                  expiryDate: LocalDateTime)
