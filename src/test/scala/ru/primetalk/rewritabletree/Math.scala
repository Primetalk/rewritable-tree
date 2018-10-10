package ru.primetalk.rewritabletree

object Math {

  sealed trait Expr
  case class Number(i: Int) extends Expr
  case class Name(name: String) extends Expr
  sealed trait Function2 extends Expr
  case object Add extends Function2
  case object Sub extends Function2
  case object Mul extends Function2
  case object Div extends Function2
  case class Application2(f: Function2, a: Expr, b: Expr) extends Expr

  trait ExprSyntax {
    import scala.language.implicitConversions

    implicit def liftSymbol(s: Symbol): Name = Name(s.name)
    implicit def liftInt(i: Int): Number = Number(i)

    implicit class Function2Ops(f: Function2){
      def apply(a: Expr, b: Expr): Application2 = Application2(f, a, b)
    }
    implicit class ExprOps(a: Expr){
      def +(b: Expr): Application2 = Add(a, b)
      def -(b: Expr): Application2 = Sub(a, b)
      def *(b: Expr): Application2 = Mul(a, b)
      def /(b: Expr): Application2 = Div(a, b)
    }
  }

  object syntax extends ExprSyntax

  implicit object RewritableExpr extends RewritableTree[Expr] {
    override def children(node: Expr): List[Expr] = node match {
      case Math.Application2(f, a, b) => List(f, a, b)
      case _ => Nil
    }

    override def replaceChildren(node: Expr, newChildren: List[Expr]): Expr = (node, newChildren) match {
      case (Math.Application2(_, _, _), List(f: Function2, a, b)) => Math.Application2(f, a, b)
      case (n, Nil) => n
      case _ => throw new IllegalArgumentException(s"Cannot replace children in $node to $newChildren")
    }
  }
}
