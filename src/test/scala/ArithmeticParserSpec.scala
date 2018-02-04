import org.scalatest.{FlatSpec, Matchers}

class ArithmeticParserSpec extends FlatSpec with Matchers {

  "Calculator" should "evaluate expressions and return correct results" in {
    Calculator.calculate("2+2*2") shouldEqual 6
    Calculator.calculate("4*(2+1)/2+5") shouldEqual 11
    Calculator.calculate("(1-1)*2+3*(1-3+4)+10/2") shouldEqual 11
  }
}
