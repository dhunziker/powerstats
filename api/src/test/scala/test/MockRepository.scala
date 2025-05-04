package dev.powerstats.api
package test

import cats.effect.kernel.Ref
import cats.effect.{IO, Ref}

trait MockRepository[T] {
  private val storage: Ref[IO, Map[Long, T]] = Ref.unsafe(Map.empty)
  private val iterator = Iterator.from(0)

  def nextId: Long = iterator.next().toLong

  def getStorage: IO[Map[Long, T]] = storage.get

  def findInStorage(predicate: T => Boolean): IO[Option[T]] = {
    for {
      currentValue <- storage.get
      result = currentValue.values.find(predicate)
    } yield result
  }

  def filterStorage(filter: T => Boolean): IO[List[T]] = {
    for {
      currentValue <- storage.get
      result = currentValue.values.filter(filter)
    } yield result.toList
  }

  def addToStorage(id: Long, value: T): IO[Unit] = {
    storage.update(_.updated(id, value))
  }

  def removeFromStorage(id: Long, predicate: T => Boolean): IO[Int] = {
    for {
      value <- findInStorage(predicate)
      count <- value.map { _ =>
        storage.update(_.removed(id)).map(_ => 1)
      }.getOrElse(IO.pure(0))
    } yield count
  }
}
