package ru.primetalk.rewritabletree

import org.scalatest.{FlatSpec, Matchers}

class RewritableTreeTest extends FlatSpec with Matchers {

  import Math._
  import Math.syntax._
  import RewritableTree.syntax._

  import RewritableExpr.RewritingRule

  def distributive: RewritingRule = {
    case Application2(Mul, a, Application2(Add, b, c)) =>
      a * b + a * c
  }

  def unit: RewritingRule = {
    case Application2(Mul, Number(1), a) =>
      a
    case Application2(Mul, a, Number(1)) =>
      a
  }

  def aFewRules: RewritingRule =
    Seq(
      distributive,
      unit
    )
      .reduce(_ orElse _)

  val x: Expr = 'x
  val expr: Expr = liftSymbol('a) * (liftSymbol('b) + Number(1))

  "rewrite" should "apply distributive property" in {
    expr.rewriteOnce(distributive) shouldBe (liftSymbol('a) * 'b + liftSymbol('a) * 1)
  }

  "rewriteUntilStable" should "apply a few rules" in {
    expr.rewriteUntilStable(aFewRules) shouldBe (liftSymbol('a) * 'b + 'a)
  }

  "collect" should "find all symbols" in {
    expr.collect{
      case Name(name) => name
    } shouldBe Seq("a", "b")
  }

  "fold" should "traverse all nodes" in {
    expr.size shouldBe 7
  }
}
