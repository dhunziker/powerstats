package ai.powerstats.api
package service.util

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class Base62HelperSpec extends AnyFlatSpec with Matchers {

  behavior of "encodeString"

  it should "encode string using base62 encoding" in {
    Base62Helper.encodeString("test") should be("289lyu")
  }

  behavior of "decodeString"

  it should "decode string encoded using base62 encoding" in {
    Base62Helper.decodeString("289lyu") should be("test")
  }
}
