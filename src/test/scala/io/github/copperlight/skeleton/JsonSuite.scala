package io.github.copperlight.skeleton

import com.typesafe.scalalogging.StrictLogging
import munit.FunSuite

import java.nio.charset.StandardCharsets

case class TestObject(paramOne: String, paramTwo: Int)

class JsonSuite extends FunSuite with StrictLogging {

  test("encode empty list") {
    val l = new String(Json.encode(Nil), StandardCharsets.UTF_8)
    assertEquals(l, "[]")
  }

  test("encode list") {
    val l = new String(Json.encode(List("a", "b")), StandardCharsets.UTF_8)
    assertEquals(l, """["a","b"]""")
  }

  test("encode empty map") {
    val m = new String(Json.encode(Map.empty), StandardCharsets.UTF_8)
    assertEquals(m, "{}")
  }

  test("encode map") {
    val m = new String(Json.encode(Map("a" -> 1, "b" -> 2)), StandardCharsets.UTF_8)
    assertEquals(m, """{"a":1,"b":2}""")
  }

  test("encode object") {
    val o = new String(Json.encode(TestObject("a", 1)), StandardCharsets.UTF_8)
    assertEquals(o, """{"paramOne":"a","paramTwo":1}""")
  }

  test("decode empty list") {
    val l = Json.decode[List[String]]("""[]""".getBytes)
    assertEquals(l, List.empty)
  }

  test("decode list") {
    val l = Json.decode[List[String]]("""["a", "b"]""".getBytes)
    assertEquals(l, List("a", "b"))
  }

  test("decode empty map") {
    val m = Json.decode[Map[String, Int]]("""{}""".getBytes)
    assertEquals(m, Map.empty[String, Int])
  }

  test("decode map") {
    val m = Json.decode[Map[String, Int]]("""{"a":1,"b":2}""".getBytes)
    assertEquals(m, Map("a" -> 1, "b" -> 2))
  }

  test("decode object") {
    val o = Json.decode[TestObject]("""{"paramOne":"a","paramTwo":1}""".getBytes)
    assertEquals(o, TestObject("a", 1))
  }

  test("decode ignores unknown properties") {
    val o = Json.decode[TestObject]("""{"paramOne":"a","paramTwo":1,"paramThree":2}""".getBytes)
    assertEquals(o, TestObject("a", 1))
  }
}
