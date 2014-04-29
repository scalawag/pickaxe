package org.scalawag.pickaxe

import scala.reflect.runtime.universe._
import scala.util.{Failure,Success,Try}

object PickaxeConversion {
  def single[IN:TypeTag,OUT:TypeTag](in:IN,out: => OUT):PickaxeConversion[IN,OUT] =
    sequence(in,Seq(out))
  def sequence[IN:TypeTag,OUT:TypeTag](in:IN,out: => Seq[OUT]):PickaxeConversion[IN,OUT] =
    PickaxeConversion[IN,OUT](in,Left(Try(out)))
  def failure[IN:TypeTag,OUT:TypeTag](in:IN,t:Throwable):PickaxeConversion[IN,OUT] =
    PickaxeConversion[IN,OUT](in,Left(Failure(t)))
  def descend[IN:TypeTag,OUT:TypeTag](in:IN,out:Seq[PickaxeConversion[IN,OUT]]):PickaxeConversion[IN,OUT] =
    PickaxeConversion[IN,OUT](in,Right(out))
}

case class PickaxeConversion[+IN:TypeTag,+OUT:TypeTag](val in:IN,val out:Either[Try[Seq[OUT]],Seq[PickaxeConversion[_,OUT]]]) {
  val inputType = typeOf[IN]
  val outputType = typeOf[OUT]

  // This will throw an exception if there are errors in any of the conversions recursively.

  private[pickaxe] lazy val items:Seq[OUT] =
    try {
      out match {
        case Right(children) => children.flatMap(_.items)
        case Left(Success(values)) => values
        case Left(Failure(_)) => throw PickaxeConversionException(this)
      }
    } catch {
      // This converts any PickaxeConversionException thrown by one of our children
      // to reflect the root of the conversion instead of the one that threw it.
      // This is to give the developer more context.
      case _:PickaxeConversionException[_,_] => throw new PickaxeConversionException(this)
    }

  private[pickaxe] lazy val toLines:Iterable[String] =
    out match {
      case Right(children) =>
        Seq(s"$in") ++ children.flatMap(_.toLines).map("  " + _)
      case Left(Success(values)) =>
        Seq(s"$in => $values")
      case Left(Failure(ex)) =>
        Seq(s"$in => FAIL: $ex")
    }

  override lazy val toString = toLines.mkString("  ","\n  ","")
}

/* pickaxe -- Copyright 2013 Justin Patterson -- All Rights Reserved */
