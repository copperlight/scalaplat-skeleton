package io.github.copperlight.skeleton

import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

object Api {
  val route: Route = {
    concat(
      path("hello") {
        get {
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to my akka friend.</h1>"))
        }
      },
      pathEndOrSingleSlash {
        get {
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, """<h1>Say <a href="/hello">/hello</a>.</h1>"""))
        }
      }
    )
  }
}
