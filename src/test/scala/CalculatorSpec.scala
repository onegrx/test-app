import akka.http.scaladsl.model.{HttpEntity, HttpMethods, HttpRequest, MediaTypes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.ByteString
import org.scalatest.{FlatSpec, Matchers}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

class CalculatorSpec extends FlatSpec with Matchers with ScalatestRouteTest with RestServer {

  val expressionSpec = ("(1-1)*2+3*(1-3+4)+10/2", 11)

  def post(expression: String): HttpRequest = {
    val jsonRequest = ByteString(
      s"""
         |{
         |    "expression":"$expression"
         |}
        """.stripMargin)

    HttpRequest(
      HttpMethods.POST,
      uri = "/evaluate",
      entity = HttpEntity(MediaTypes.`application/json`, jsonRequest))
  }

  "Calculator" should "evaluate expressions and return correct results" in {
    Calculator.calc("2+2*2") shouldEqual 6
    Calculator.calc("4*(2+1)/2+5") shouldEqual 11
    Calculator.calc(expressionSpec._1) shouldEqual expressionSpec._2
  }

  "The server" should "return proper results via HTTP" in {
    post(expressionSpec._1) ~> route ~> check {
      status.isSuccess() shouldEqual true
      responseAs[ExpressionOutput].result shouldEqual expressionSpec._2
    }
    post("1/2") ~> route ~> check {
      status.isSuccess() shouldEqual true
      responseAs[ExpressionOutput].result shouldEqual 0.5
    }
  }
}
