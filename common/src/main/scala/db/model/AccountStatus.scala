package ai.powerstats.common
package db.model

import doobie.Meta

enum AccountStatus {
  case Provisional, Verified, Suspended
}

object AccountStatus:
  implicit val accountStatusMeta: Meta[AccountStatus] = Meta[String].imap(AccountStatus.valueOf)(_.toString)
end AccountStatus