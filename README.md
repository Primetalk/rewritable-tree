# Rewritable tree

This library contains some tools for working with 
immutable data structures represented with algebraic data types.
These data structures can be thought of as a tree.
One of the tools is the `rewrite` function that helps 
to optimize the data structure based on some rules.

## Primer

Let's try to implement a distributive property of elementary algebra:

    a * (b + c) == a * b + a * c

We can model arithmetic expressions using the following data structure:

    sealed trait Expr
    case class Number(i:Int) extends Expr
    case class Add(a: Expr, b: Expr) extends Expr
    case class Mul(a: Expr, b: Expr) extends Expr

The distributive property (`a * (b + c) == a * b + a * c`) can be illustrated with trees:

       Mul              Add
       / \              / \
      a  Add    ==   Mul  Mul
         / \         / \  / \
        b  c        a  b a  c

and it can implemented as the following pattern matching rule:

    case Mul(a@_, Add(b@_, c@_)) => Add(Mul(a, b), Mul(a, c))

If we want to apply this rule through the whole expression 
we need a way to traverse the tree and reconstruct it in 
case of replacement.

    def rewrite(rule: Expr => Option[Expr])(tree: Expr): Expr
      
It's analogous to `Functor.map` with the difference that 
we are not mapping the data inside the tree but rather 
the "spine", the structure of the tree.

## Fold (aka catamorphism)

We can construct another data structure from our tree, or calculate 
some statistics about it.

    def fold[A](zero: A)(f: A => Expr => A): A
    
For instance, to count tree nodes:

    val count = fold(0)(i => _ => i + 1)

There are two options for fold - whether we traverse 
the tree depth-first or width-first.

## Collect data

Apart from rewriting trees it's often the case when we want to
collect some data from the tree. This can be achieved with 
`collect` method

    def collect[A](tree: Expr)(pf: PartialFunction[Expr, A]): Seq[A]

It can be used when we want to collect some of the tree elements.
