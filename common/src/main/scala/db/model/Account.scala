package ai.powerstats.common
package db.model

import java.time.LocalDateTime

case class Account(id: Long,
                   email: String,
                   passwordHash: Array[Byte],
                   status: AccountStatus,
                   creationDate: LocalDateTime)
