package org.scalawag.pickaxe.sdom

import scala.reflect.runtime.universe._
import org.scalawag.pickaxe.{PickaxeConversion, Pickaxe}
import org.jdom2._
import PickaxeConversion._

object SdomPickaxe extends SdomPickaxe(false)

class SdomPickaxe(val recursive:Boolean = false) extends Pickaxe[Iterable[AnyRef]](recursive,false) {
  private val STRING:Type = typeOf[String]
  private val INT:Type = typeOf[Int]
  private val LONG:Type = typeOf[Long]
  private val FLOAT:Type = typeOf[Float]
  private val DOUBLE:Type = typeOf[Double]
  private val BOOLEAN:Type = typeOf[Boolean]
  private val ELEMENT:Type = typeOf[Element]

  override protected def convert = {
    case (in,STRING)  => string(in)
    case (in,INT)     => int(in)
    case (in,LONG)    => long(in)
    case (in,FLOAT)   => float(in)
    case (in,DOUBLE)  => double(in)
    case (in,BOOLEAN) => boolean(in)
    case (in,ELEMENT) => elem(in)
  }

/* TODO: get support for recursion working here
  override protected def extractRecursive[OUT:TypeTag](extract: => Extractor[OUT]):Extractor[OUT] = {
    // This is a complex element (contains children which are elements).  Recurse (if allowed) by
    // converting each of the child elements individually.
    case ins:Iterable[_] if ins.exists( e => e.isInstanceOf[Element] ) =>
      val es = ins.filter(_.isInstanceOf[Element]).map(_.asInstanceOf[Element])
      val convs = es map { e =>
        extract(Iterable(e))
      }
      PickaxeConversion[Iterable[AnyRef],OUT](ins,Right(convs.toSeq))
  }
*/

  def string(in:Iterable[AnyRef]) = textContent(in)(identity)
  def int(in:Iterable[AnyRef]) = textContent(in)(_.trim.toInt)
  def long(in:Iterable[AnyRef]) = textContent(in)(_.trim.toLong)
  def float(in:Iterable[AnyRef]) = textContent(in)(_.trim.toFloat)
  def double(in:Iterable[AnyRef]) = textContent(in)(_.trim.toDouble)
  def boolean(in:Iterable[AnyRef]) = textContent(in)(_.trim.toBoolean)

  def elem(in:Iterable[AnyRef]) = process(in) {
    case seq if seq.forall(_.isInstanceOf[Element]) =>
      sequence[Iterable[AnyRef],Element](seq,seq.toSeq.map(_.asInstanceOf[Element]))
  }

  private[this] def textContent[OUT:TypeTag](in:Iterable[AnyRef])(fromString:String => OUT):PickaxeConversion[Iterable[AnyRef],OUT] =
    process(in) {
      case seq =>
        sequence[Iterable[AnyRef],OUT](seq,seq.toSeq.map {
          case e:Element if e.getChildren.isEmpty => fromString(e.getText)
          case a:Attribute => fromString(a.getValue)
          case t:Text => fromString(t.getText)
        })
    }
}

/* pickaxe -- Copyright 2013 Justin Patterson -- All Rights Reserved */
