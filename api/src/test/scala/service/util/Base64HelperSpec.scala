package dev.powerstats.api
package service.util

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class Base64HelperSpec extends AnyFlatSpec with Matchers {

  behavior of "encodeString"

  it should "encode string using base62 encoding" in {
    Base64Helper.encodeString("test") should be("dGVzdA")
  }

  behavior of "decodeString"

  it should "decode string encoded using base62 encoding" in {
    Base64Helper.decodeString("dGVzdA") should be("test")
  }
}
