package ru.primetalk.rewritabletree

import simulacrum.typeclass

import scala.collection.mutable
import scala.collection.generic.CanBuildFrom
import scala.collection.immutable.Queue

@typeclass trait RewritableTree[T] {
  def children(node: T): List[T]
  def replaceChildren(node: T, newChildren: List[T]): T

  type RewritingRule = PartialFunction[T, T]
}

object RewritableTree {

  def apply[T: RewritableTree](t: T): RewritableTree[T] = implicitly[RewritableTree[T]]

  def rewriteOnce[T: RewritableTree](pf: PartialFunction[T, T]): T => T = t => {
    rewrite0(pf)(t).getOrElse(t)
  }

  def rewriteUntilStable[T: RewritableTree](f: PartialFunction[T, T])(t: T): T = {
    rewrite0(f)(t) match {
      case None => t
      case Some(t0) => rewriteUntilStable(f)(t0)
    }
  }

  // rewriting algorithm
  private def rewrite0[T: RewritableTree](f: PartialFunction[T, T])(t: T): Option[T] = {
    import RewritableTree.ops._
    //  val rt = implicitly[RewritableTree[T]]
    val children = t.children // rt.children(t)
    var changed = false
    val updatedChildren = children.map{child =>
      val res = rewrite0(f)(child)
      changed = changed || res.isDefined
      res.getOrElse(child)
    }
    val t1 =
      if(changed)
        t.replaceChildren(updatedChildren)
      else
        t
    var changed2 = true
    val t2 = f.applyOrElse(t1, (_:T) =>{changed2 = false; t1})
    if(changed || changed2)
      Some(t2)
    else
      None
  }

  def collectWidthFirst[T: RewritableTree, B, That](pf: PartialFunction[T, B])(implicit cbf: CanBuildFrom[_, B, That]): T => That =
    collectWidthFirstOpt(pf.lift)

  def collectWidthFirstOpt[T: RewritableTree, B, That](fo: Function[T, Option[B]])(implicit cbf: CanBuildFrom[_, B, That]): T => That =
    t => {
      foldWidthFirst[T, mutable.Builder[B, That]](cbf.apply()) {
        (b, x) =>
          fo(x).foreach(b += _)
          b
      }
        .apply(t)
        .result()
    }

  def foldWidthFirst[T: RewritableTree, B](z: B)(op: (B, T) => B): T => B = {
    val rt = RewritableTree[T]
    def go(acc: B, nodes: Queue[T]): B =
      if(nodes.isEmpty)
        acc
      else
      {
        val (x, xs) = nodes.dequeue
        val b = op(acc, x)
        val children = rt.children(x)
        go(b, xs.enqueue(children))
      }

    t => go(z, Queue(t))
  }

  def foldDepthFirst[T: RewritableTree, B](z: B)(op: (B, T) => B): T => B = {
    val rt = RewritableTree[T]
    def go(acc: B, t: T): B = {
      val b1 = op(acc, t)
      val children = rt.children(t)
      children.foldLeft(b1)(go)
    }

    t => go(z, t)
  }

  trait RewritableTreeSyntax {

    implicit class RewritableTreeOps[T: RewritableTree](tree: T) {
      def collect[B, That](pf: PartialFunction[T, B])(implicit cbf: CanBuildFrom[_, B, That]): That =
        RewritableTree.collectWidthFirst[T, B, That](pf).apply(tree)

      def rewriteOnce(pf: PartialFunction[T, T]): T =
        RewritableTree.rewriteOnce(pf).apply(tree)

      def rewriteUntilStable(pf: PartialFunction[T, T]): T =
        RewritableTree.rewriteUntilStable[T](pf)(tree)

      def foldWidthFirst[B](z: B)(op: (B, T) => B): B =
        RewritableTree.foldWidthFirst(z)(op).apply(tree)

      def size: Int =
        foldWidthFirst(0)((cnt, _) => cnt + 1)
    }
  }
  object syntax extends RewritableTreeSyntax
}