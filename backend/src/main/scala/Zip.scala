package dev.powerstats.backend

import cats.effect.IO
import cats.implicits.*

import java.io.FileOutputStream
import java.nio.file.Path
import java.util.zip.{ZipEntry, ZipFile}
import scala.jdk.CollectionConverters.*

object Zip {
  private val BufferSize = 4096

  def unzip(file: Path): IO[Unit] = {
    for {
      _ <- IO(println(s"Unzipping ${file.getFileName}"))
      _ <- IO(new ZipFile(file.toFile)).bracket { zipFile =>
        for {
          iter <- IO(zipFile.entries.asScala
            .takeWhile(_ != null)
            .map { zipEntry =>
              if (zipEntry.isDirectory) {
                handleDirectory(file.getParent, zipEntry)
              } else {
                handleFile(file.getParent, zipFile, zipEntry)
              }
            })
          _ <- iter.toList.sequence
        } yield ()
      } { zipFile =>
        IO(zipFile.close())
      }
      _ <- IO(println(s"${file.getFileName} unzipped successfully"))
    } yield ()
  }

  private def handleDirectory(targetDir: Path, zipEntry: ZipEntry): IO[Unit] = {
    IO(targetDir.resolve(zipEntry.getName).toFile
      .mkdirs())
      .flatMap { success =>
        IO.raiseUnless(success)(new RuntimeException("Failed to create directory"))
      }
  }

  // Refactor this method to use IO
  private def handleFile(targetDir: Path, zipFile: ZipFile, zipEntry: ZipEntry): IO[Unit] = IO {
    val fos = new FileOutputStream(targetDir.resolve(zipEntry.getName).toFile)
    val is = zipFile.getInputStream(zipEntry)
    val buffer = new Array[Byte](BufferSize)
    var len = is.read(buffer)
    while (len > 0) {
      fos.write(buffer, 0, len)
      len = is.read(buffer)
    }
    fos.close()
    println(s"${zipEntry.getName} unpacked")
  }
}
