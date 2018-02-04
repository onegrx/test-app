import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.RootJsonFormat
import spray.json.DefaultJsonProtocol._

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

object Server {

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  def main(args: Array[String]): Unit = {

    implicit val inputFormat: RootJsonFormat[ExpressionInput] = jsonFormat1(ExpressionInput)

    val route: Route =
      post {
        path("evaluate") {
          entity(as[ExpressionInput]) { expr =>
            complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, Calculator.calculate(expr.expression).toString))
          }
        }
      }

    val bindingFuture = Http().bindAndHandle(route, "localhost", 5555)
    println(s"Server online at http://localhost:5555/\nPress RETURN to stop...")
    StdIn.readLine()
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }


}
