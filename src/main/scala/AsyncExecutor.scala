import akka.NotUsed
import akka.stream._
import akka.stream.scaladsl._

import scala.concurrent.Future

object AsyncExecutor {

  type Transformation = (Double, Double) => Double
  type SourceDouble = Source[Double, NotUsed]

  def execute(treeExp: TreeExp): RunnableGraph[Future[Double]] =
    toGraph(treeExp).toMat(Sink.head[Double])(Keep.right)

  def createGraphFromTree(xExp: TreeExp, yExp: TreeExp, t: Transformation): SourceDouble = {
    (xExp, yExp) match {
      case (Num(x), Num(y)) => getGraph(Source.single(x), Source.single(y), t)
      case (Num(x), y) => getGraph(Source.single(x), toGraph(y), t)
      case (x, Num(y)) => getGraph(toGraph(x), Source.single(y), t)
      case (x, y) => getGraph(toGraph(x), toGraph(y), t)
    }
  }

  def toGraph(exp: TreeExp): SourceDouble = exp match {
    case Add(x, y) => createGraphFromTree(x, y, _ + _)
    case Sub(x, y) => createGraphFromTree(x, y, _ - _)
    case Mul(x, y) => createGraphFromTree(x, y, _ * _)
    case Div(x, y) => createGraphFromTree(x, y, _ / _)
    case Num(x) => Source.single(x)
  }

  def getGraph(xSource: SourceDouble, ySource: SourceDouble, t: Transformation) = {
    Source.fromGraph(GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._
      val zip = builder.add(ZipWith[Double, Double, Double](t))
      xSource ~> zip.in0
      ySource ~> zip.in1
      SourceShape(zip.out)
    }.async)
  }
}
