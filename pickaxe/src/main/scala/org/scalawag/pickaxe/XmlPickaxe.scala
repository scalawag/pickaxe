package org.scalawag.pickaxe

import scala.reflect.runtime.universe._
import scala.xml._
import PickaxeConversion._

object XmlPickaxe extends XmlPickaxe(false)

class XmlPickaxe(val recursive:Boolean = false) extends Pickaxe[NodeSeq](recursive,false) {
  private val STRING:Type = typeOf[String]
  private val INT:Type = typeOf[Int]
  private val LONG:Type = typeOf[Long]
  private val FLOAT:Type = typeOf[Float]
  private val DOUBLE:Type = typeOf[Double]
  private val BOOLEAN:Type = typeOf[Boolean]
  private val ELEM:Type = typeOf[Elem]

  override protected def convert = {
    case (in,STRING)  => string(in)
    case (in,INT)     => int(in)
    case (in,LONG)    => long(in)
    case (in,FLOAT)   => float(in)
    case (in,DOUBLE)  => double(in)
    case (in,BOOLEAN) => boolean(in)
    case (in,ELEM)    => elem(in)
  }

  override protected def extractRecursive[OUT:TypeTag](extract: => Extractor[OUT]):Extractor[OUT] = {
    // This is a complex element (contains children which are elements).  Recurse (if allowed) by
    // converting each of the child elements individually.
    case x:Elem if x.child.exists(_.isInstanceOf[Elem])=>
      PickaxeConversion[NodeSeq,OUT](x,Right(x.child.filter(_.isInstanceOf[Elem]).map(extract)))
  }

  override protected def extractCommon[OUT:TypeTag](extract: => Extractor[OUT]):Extractor[OUT] = {
    // This is a NodeSeq that's not a Node.  Process all the elements individually.
    case x:NodeSeq if ! x.isInstanceOf[Node] =>
      PickaxeConversion[NodeSeq,OUT](x,Right(x.map(extract)))
  }

  def string(in:NodeSeq) = textContent(in)(identity)
  def int(in:NodeSeq) = textContent(in)(_.trim.toInt)
  def long(in:NodeSeq) = textContent(in)(_.trim.toLong)
  def float(in:NodeSeq) = textContent(in)(_.trim.toFloat)
  def double(in:NodeSeq) = textContent(in)(_.trim.toDouble)
  def boolean(in:NodeSeq) = textContent(in)(_.trim.toBoolean)

  def elem(in:NodeSeq) = process(in) {
    case x:Elem => single(x,x)
  }

  private[this] def textContent[OUT:TypeTag](in:NodeSeq)(fromString:String => OUT):PickaxeConversion[NodeSeq,OUT] =
    process(in) {
      // For elements that have only text children, we don't consider this a recursion.  Just take
      // the text from all the children and try to convert it to the correct type using the fromString
      // function that was passed in.
      case x:Elem if x.child.forall( ! _.isInstanceOf[Elem] ) => single(x,fromString(x.text))
      // For NodeSeqs that contain only text Atoms, act similar to the above, only we're look at
      // the Nodes in the sequence itself as opposed to the child node (and don't collapse).
      case x if x.length > 0 && x.forall(_.isInstanceOf[Atom[_]]) => sequence[NodeSeq,OUT](x,x.map(_.text).map(fromString))
    }

  implicit class AttributeExtractor(ns:NodeSeq) {
    def \@(s:String) = ns.flatMap( _ \ s"@$s" )
  }
}

/* pickaxe -- Copyright 2013 Justin Patterson -- All Rights Reserved */
