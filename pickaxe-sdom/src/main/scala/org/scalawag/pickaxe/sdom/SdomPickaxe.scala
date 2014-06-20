package org.scalawag.pickaxe.sdom

import scala.reflect.runtime.universe._
import org.scalawag.pickaxe.{PickaxeConversion, Pickaxe}
import org.scalawag.sdom._
import PickaxeConversion._

object SdomPickaxe extends SdomPickaxe(false)

class SdomPickaxe(val recursive:Boolean = false) extends Pickaxe[Iterable[Node]](recursive,false) {
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
    case (in,ELEMENT) => element(in)
  }

  override protected def extractRecursive[OUT:TypeTag](extract: => Extractor[OUT]):Extractor[OUT] = {
    case x:Iterable[Node] if containsOnlyWhitespaceAndElems(x) =>
      PickaxeConversion[Iterable[Node],OUT](x,Right(elementsOnly(x).map( e => extract(e.children) ).toSeq))
  }

  def string(in:Iterable[Node]) = textContent(in)(identity)
  def int(in:Iterable[Node]) = textContent(in)(_.trim.toInt)
  def long(in:Iterable[Node]) = textContent(in)(_.trim.toLong)
  def float(in:Iterable[Node]) = textContent(in)(_.trim.toFloat)
  def double(in:Iterable[Node]) = textContent(in)(_.trim.toDouble)
  def boolean(in:Iterable[Node]) = textContent(in)(_.trim.toBoolean)

  def element(in:Iterable[Node]) = process(in) {
    case x:Iterable[Node] if containsOnlyWhitespaceAndElems(x) =>
      sequence[Iterable[Node],Element](x,elementsOnly(x).toSeq)
  }

  private[this] def textContent[OUT:TypeTag](in:Iterable[Node])(fromString:String => OUT):PickaxeConversion[Iterable[Node],OUT] =
    process(in) {
      case x:Iterable[Node] if x.forall(_.isInstanceOf[Attribute]) =>
        sequence[Iterable[Node],OUT](x,x.map(_.asInstanceOf[Attribute].value).map(fromString).toSeq)
      // For elements that have only text children, we don't consider this a recursion.  Just take
      // the text from all the children and try to convert it to the correct type using the fromString
      // function that was passed in.  It may appear in an Iterable if it was the result of a navigation.
      case x:Iterable[Node] if containsOnlySimpleElements(x) =>
        sequence[Iterable[Node],OUT](x,elementsOnly(x).map(_.text).map(fromString).toSeq)
    }

  private[this] def removeInsignificants(x:Iterable[Node]) =
    x filter {
      case n:Element => true
      case t:TextLike if ! t.text.trim.isEmpty => true
      case _ => false
    }

  private[this] def containsOnlyWhitespaceAndElems(g:Iterable[Node]) =
    g.forall( n => n.isInstanceOf[TextLike] && n.asInstanceOf[TextLike].text.trim.isEmpty || n.isInstanceOf[Element] )

  private[this] def isSimpleElement(node:Node) =
    node.isInstanceOf[Element] && node.asInstanceOf[Element].children.forall(! _.isInstanceOf[Element])

  private[this] def containsOnlySimpleElements(nodes:Iterable[Node]) =
    removeInsignificants(nodes).forall(isSimpleElement)

  private[this] def elementsOnly(nodes:Iterable[Node]):Iterable[Element] =
    nodes.filter(_.isInstanceOf[Element]).map(_.asInstanceOf[Element])
}

/* pickaxe -- Copyright 2013 Justin Patterson -- All Rights Reserved */
