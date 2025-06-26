package dev.powerstats.common
package db.model

import db.model.AccountStatus.Verified

import java.time.LocalDateTime

case class Account(id: Long,
                   email: String,
                   passwordHash: Array[Byte],
                   status: AccountStatus,
                   creationDate: LocalDateTime) {
  def isActivated = status == Verified
}
