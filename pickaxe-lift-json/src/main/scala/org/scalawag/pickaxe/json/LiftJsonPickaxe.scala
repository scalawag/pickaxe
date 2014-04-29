package org.scalawag.pickaxe.json

import scala.reflect.runtime.universe._
import net.liftweb.json.JsonAST._
import scala.math.BigInt
import org.scalawag.pickaxe.Pickaxe
import org.scalawag.pickaxe.PickaxeConversion._

object LiftJsonPickaxe extends LiftJsonPickaxe(true,false)

class LiftJsonPickaxe(private val recursive:Boolean = true,private val lax:Boolean = false) extends Pickaxe[JValue](recursive,lax) {
  private[this] val ZERO = BigInt(0)

  private val STRING = typeOf[String]
  private val INT = typeOf[Int]
  private val LONG = typeOf[Long]
  private val FLOAT = typeOf[Float]
  private val DOUBLE = typeOf[Double]
  private val BOOLEAN = typeOf[Boolean]
  private val JOBJECT = typeOf[JObject]
  private val JARRAY = typeOf[JArray]

  override protected def convert = {
    case (in,STRING)  => string(in)
    case (in,INT)     => int(in)
    case (in,LONG)    => long(in)
    case (in,FLOAT)   => float(in)
    case (in,DOUBLE)  => double(in)
    case (in,BOOLEAN) => boolean(in)
    case (in,JOBJECT) => jobject(in)
    case (in,JARRAY)  => jarray(in)
  }

  override protected def extractRecursive[OUT:TypeTag](extract: => Extractor[OUT]) = {
    case x @ JField(_,value) => descend(x,Seq(extract(value)))
    case x @ JArray(items) => descend(x,items.map(extract))
    case x @ JObject(fields) => descend(x,fields.map(extract))
  }

  override protected def extractCommon[OUT:TypeTag](extract: => Extractor[OUT]) = {
    case x @ JNothing => sequence(x,Seq())
  }

  def string(in:JValue) = process(in)(
    strict = {
      case x @ JString(s) => single(x,s)
    },
    lax = {
      case x @ JInt(n) => single(x,n.toString)
      case x @ JBool(b) => single(x,b.toString)
      case x @ JDouble(d) => single(x,d.toString)
      case x @ JNull => single(x,"null")
    }
  )

  def int(in:JValue) = process(in)(
    strict = {
      case x @ JInt(n) => single(x,n.toInt)
    },
    lax = {
      case x @ JString(s) => single(x,s.toInt)
      case x @ JBool(b) => single(x,if (b) 1 else 0)
      case x @ JDouble(d) => single(x,d.toInt)
      case x @ JNull => single(x,0)
    }
  )

  def long(in:JValue) = process(in)(
    strict = {
      case x @ JInt(n) => single(x,n.toLong)
    },
    lax = {
      case x @ JString(s) => single(x,s.toLong)
      case x @ JBool(b) => single(x,if (b) 1L else 0L)
      case x @ JDouble(d) => single(x,d.toLong)
      case x @ JNull => single(x,0L)
    }
  )

  def float(in:JValue) = process(in)(
    strict = {
      case x @ JDouble(d) => single(x,d.toFloat)
    },
    lax = {
      case x @ JInt(n) => single(x,n.toFloat)
      case x @ JString(s) => single(x,s.toFloat)
      case x @ JBool(b) => single(x,if (b) 1f else 0f)
      case x @ JNull => single(x,0f)
    }
  )

  def double(in:JValue) = process(in)(
    strict = {
      case x @ JDouble(d) => single(x,d.toDouble)
    },
    lax = {
      case x @ JInt(n) => single(x,n.toDouble)
      case x @ JString(s) => single(x,s.toDouble)
      case x @ JBool(b) => single(x,if (b) 1.0 else 0.0)
      case x @ JNull => single(x,0.0)
    }
  )

  def boolean(in:JValue) = process(in)(
    strict = {
      case x @ JBool(b) => single(x,b)
    },
    lax = {
      case x @ JDouble(d) => single(x,d != 0.0)
      case x @ JInt(n) => single(x,n != ZERO)
      case x @ JString(s) => single(x,s.toBoolean)
      case x @ JNull => single(x,false)
    }
  )

  def jobject(in:JValue) = process(in) {
    case o:JObject => single(o,o)
  }

  def jarray(in:JValue) = process(in) {
    case a:JArray => single(a,a)
  }
}

/* pickaxe -- Copyright 2013 Justin Patterson -- All Rights Reserved */
