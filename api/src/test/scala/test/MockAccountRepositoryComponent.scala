package ai.powerstats.api
package test

import ai.powerstats.common.db.AccountRepositoryComponent
import ai.powerstats.common.db.model.{Account, AccountStatus}
import cats.effect.IO
import doobie.Transactor
import fs2.io.file.{Files, Path}
import io.circe.*
import io.circe.generic.auto.*
import org.http4s.dsl.io.*

import java.nio.charset.StandardCharsets
import scala.util.Try

trait MockAccountRepositoryComponent extends AccountRepositoryComponent {
  val accountRepository: AccountRepository
  val insertCounts: Iterator[Int]

  trait MockAccountRepository extends AccountRepository {
    override def findAccount(email: String, xa: Transactor[IO]): IO[Option[Account]] = {
      implicit val decodePasswordHash: Decoder[Array[Byte]] = Decoder.decodeString.emapTry { str =>
        Try(str.getBytes(StandardCharsets.UTF_8))
      }
      implicit val decodeStatus: Decoder[AccountStatus] = Decoder.decodeString.emapTry { str =>
        Try(AccountStatus.valueOf(str))
      }
      val resource = getClass.getResource("/TestAccounts.json")
      Files[IO].readAll(Path(resource.getPath))
        .through(fs2.text.utf8.decode)
        .compile
        .foldMonoid
        .map(parser.decode[List[Account]])
        .flatMap {
          case Left(error) => IO.raiseError(error)
          case Right(accounts) => IO.pure(accounts.find(_.email == email))
        }
    }

    override def insertAccount(email: String, passwordHash: Array[Byte], xa: doobie.Transactor[IO]) = {
      IO.pure(insertCounts.next())
    }
  }
}
