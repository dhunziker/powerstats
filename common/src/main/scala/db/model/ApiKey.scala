package ai.powerstats.common
package db.model

import java.time.LocalDateTime

case class ApiKey(id: Long, 
                  accountId: Long, 
                  key: String, 
                  creationDate: LocalDateTime, 
                  expiryDate: LocalDateTime) {
  def isValid: Boolean = {
    val now = LocalDateTime.now()
    now.isAfter(creationDate) && now.isBefore(expiryDate)
  }
}
