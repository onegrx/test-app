import org.scalatest.{FlatSpec, Matchers}

class ArithmeticParserSpec extends FlatSpec with Matchers {

  "Calculator" should "evaluate expressions and return correct results" in {
    Calculator.calc("2+2*2") shouldEqual 6
    Calculator.calc("4*(2+1)/2+5") shouldEqual 11
    Calculator.calc("(1-1)*2+3*(1-3+4)+10/2") shouldEqual 11
  }
}
