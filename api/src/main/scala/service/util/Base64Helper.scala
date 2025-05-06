package dev.powerstats.api
package service.util

import java.nio.charset.StandardCharsets
import java.util.Base64

object Base64Helper {
  private val encoder = Base64.getUrlEncoder.withoutPadding()
  private val decoder = Base64.getUrlDecoder
  private val charset = StandardCharsets.UTF_8

  def encodeString(input: String): String = {
    encoder.encodeToString(input.getBytes(charset))
  }

  def decodeString(encoded: String): String = {
    String(decoder.decode(encoded), charset)
  }
}
