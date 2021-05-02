package io.github.copperlight.skeleton

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

object Main extends StrictLogging {

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "my-system")
    implicit val ec: ExecutionContextExecutor = system.executionContext

    val bindingFuture = Http().newServerAt("localhost", 8080).bind(Api.route)

    logger.info(s"Server online at http://localhost:8080/\nPress RETURN to stop...")

    StdIn.readLine() // run until user presses return

    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // shutdown when done
  }
}
