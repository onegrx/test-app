import scala.util.parsing.combinator.JavaTokenParsers

object Calculator {

  lazy val parsers = new ArithmeticParsers

  def eval(te: TreeExp): Double = te match {
    case Add(x, y) => eval(x) + eval(y)
    case Sub(x, y) => eval(x) - eval(y)
    case Mul(x, y) => eval(x) * eval(y)
    case Div(x, y) => eval(x) / eval(y)
    case Num(t) => t
  }

  def calculate(expression: String) = eval(parsers.parseTree(expression).get)

}

class ArithmeticParsers extends JavaTokenParsers {

  def expr: Parser[TreeExp] = term ~ rep(("+" | "-") ~ term) ^^ {
    case e ~ rest => rest.foldLeft(e) {
      case (x, "+" ~ y) => Add(x, y)
      case (x, "-" ~ y) => Sub(x, y)
    }
  }

  def term: Parser[TreeExp] = factor ~ rep(("*" | "/") ~ factor) ^^ {
    case e ~ rest => rest.foldLeft(e) {
      case (x, "*" ~ y) => Mul(x, y)
      case (x, "/" ~ y) => Div(x, y)
    }
  }

  def factor = num | "(" ~> expr <~ ")"

  def num = floatingPointNumber ^^ { n => Num(n.toDouble) }

  def parseTree(expression: String) = parseAll(expr, expression)

}

sealed abstract class TreeExp

case class Add(l: TreeExp, r: TreeExp) extends TreeExp
case class Sub(l: TreeExp, r: TreeExp) extends TreeExp
case class Mul(l: TreeExp, r: TreeExp) extends TreeExp
case class Div(l: TreeExp, r: TreeExp) extends TreeExp
case class Num(n: Double) extends TreeExp