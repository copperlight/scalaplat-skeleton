package io.github.copperlight.skeleton

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.ExceptionHandler
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.RouteTest
import akka.http.scaladsl.testkit.TestFrameworkInterface
import io.github.copperlight.skeleton.Api
import munit.FunSuite

class ApiSuite extends FunSuite with RouteTest with TestFrameworkInterface {

  override def failTest(msg: String): Nothing = {
    fail(msg)
  }

  override def testExceptionHandler: ExceptionHandler = ExceptionHandler {
    case e: Exception => throw e
    case e: AssertionError => throw e
  }

  test("GET index recommends /hello endpoint") {
    Get() ~> Api.route ~> check {
      assert(responseAs[String].contains("/hello"))
    }
  }

  test("GET /hello mentions my akka friend") {
    Get("/hello") ~> Api.route ~> check {
      assert(responseAs[String].contains("my akka friend"))
    }
  }

  test("GET other paths are unhandled") {
    Get("/scoobydoo") ~> Api.route ~> check {
      assert(!handled)
    }
  }

  test("PUT index returns MethodNotAllowed") {
    Put() ~> Route.seal(Api.route) ~> check {
      assertEquals(status, StatusCodes.MethodNotAllowed)
      assertEquals(responseAs[String], "HTTP method not allowed, supported methods: GET")
    }
  }
}
