package dev.powerstats.api
package service.util

import java.nio.charset.StandardCharsets

object Base62Helper {
  private val charset = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray
  private val base = charset.length

  def encodeString(input: String): String = {
    val bytes = input.getBytes(StandardCharsets.UTF_8)
    val number = BigInt(1, bytes)
    encode(number)
  }

  def decodeString(encoded: String): String = {
    val number = decode(encoded)
    val bytes = number.toByteArray.dropWhile(_ == 0)
    new String(bytes, StandardCharsets.UTF_8)
  }

  private def encode(number: BigInt): String = {
    require(number >= 0, "Number must be non-negative")
    var n = number
    val encoded = new StringBuilder
    while (n > 0) {
      encoded.insert(0, charset((n % base).toInt))
      n /= base
    }
    if (encoded.isEmpty) "0" else encoded.toString()
  }

  private def decode(encoded: String): BigInt = {
    encoded.foldLeft(BigInt(0)) { (acc, char) =>
      acc * base + charset.indexOf(char)
    }
  }
}
