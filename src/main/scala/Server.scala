import java.math.RoundingMode
import java.text.DecimalFormat

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import spray.json.DefaultJsonProtocol._
import spray.json.{JsNumber, JsObject, JsValue, RootJsonFormat}

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn
import scala.util.{Failure, Success}

final case class ExpressionInput(expression: String)

final case class ExpressionOutput(result: Double)

trait RestServer {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer

  implicit val inputFormat: RootJsonFormat[ExpressionInput] = jsonFormat1(ExpressionInput)

  implicit object outputFormat extends RootJsonFormat[ExpressionOutput] {

    val df = new DecimalFormat("###.#")
    df.setRoundingMode(RoundingMode.HALF_UP)

    override def write(obj: ExpressionOutput) = JsObject("result" -> JsNumber(df.format(obj.result)))

    override def read(json: JsValue) = {
      json.asJsObject.getFields("result") match {
        case Seq(JsNumber(n)) => ExpressionOutput(n.toDouble)
        case _ => spray.json.deserializationError("Can't read input")
      }
    }

  }

  val route: Route =
    post {
      path("evaluate") {
        entity(as[ExpressionInput]) { expr =>
          Calculator.calculateAsync(expr.expression) match {
            case Success(res) =>  onComplete(res) {
              case Success(result) =>
                if (result.isInfinite) complete(StatusCodes.UnprocessableEntity)
                else complete(ExpressionOutput(result))
              case Failure(_) => complete(StatusCodes.InternalServerError)
            }
            case Failure(_) => complete(StatusCodes.UnprocessableEntity)
          }
        }
      }
    }
}

object Server extends RestServer {

  override implicit val system: ActorSystem = ActorSystem()
  override implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  def main(args: Array[String]): Unit = {

    val bindingFuture = Http().bindAndHandle(route, "localhost", 5555)
    println(s"Server online at http://localhost:5555/\nPress RETURN to stop...")
    StdIn.readLine()
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }

}
