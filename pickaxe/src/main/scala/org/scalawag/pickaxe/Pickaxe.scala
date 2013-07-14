package org.scalawag.pickaxe

import scala.reflect.runtime.universe._
import scala.util.Failure
import org.scalawag.timber.api.style.slf4j.Logging
import java.io.PrintWriter

private object Pickaxe {
  private val SEQUENCE_TYPE = typeOf[Seq[Any]]
  private val OPTION_TYPE = typeOf[Option[Any]]
  private val NOTHING_TYPE = typeOf[Nothing]

  private object Cardinality extends Enumeration {
    val SEQUENCE = Value
    val OPTIONAL = Value
    val REQUIRED = Value
  }
}

import Pickaxe._

class Pickaxe[IN:TypeTag](private val recursive:Boolean = true,private val lax:Boolean = false) extends Logging {

  /** Override to provide a dispatcher for reflective conversions.  The inputs will be the data and the desired
    * type.  The function should return the PickaxeConversion if it's possible and should not be defined if it's not
    * possible.
    */

  protected def convert:PartialFunction[(IN,Type),PickaxeConversion[IN,Any]] = PartialFunction.empty

  private def fail[OUT:TypeTag]:PartialFunction[(IN,Type),PickaxeConversion[IN,OUT]] = {
    case (in,targetType) =>
      val hint = targetType match {
        case NOTHING_TYPE => " - you probably need to specify a type parameter to your mine call"
        case _ => ""
      }
      throw new IllegalArgumentException(s"conversion to $targetType not supported by this pickaxe$hint")
  }

  def mine[OUT:TypeTag](in:IN):OUT = {
    import Cardinality._

    val tpe = typeOf[OUT]

    val (cardinality,innerType) =
      if ( tpe.erasure =:= SEQUENCE_TYPE )
        (SEQUENCE,tpe.asInstanceOf[TypeRef].args.head)
      else if ( tpe.erasure =:= OPTION_TYPE )
        (OPTIONAL,tpe.asInstanceOf[TypeRef].args.head)
      else
        (REQUIRED,tpe)

    val fn = convert orElse fail[OUT]

    val conversion = fn(in,innerType).asInstanceOf[PickaxeConversion[IN,OUT]]

    val answer =
      cardinality match {
        case OPTIONAL => optional[OUT](conversion)
        case REQUIRED => required[OUT](conversion)
        case SEQUENCE => all[OUT](conversion)
      }

    answer.asInstanceOf[OUT]
  }

  private def dump[IN,OUT:TypeTag](conversion:PickaxeConversion[IN,OUT],answer:Any) {
    log.debug { pw:PrintWriter =>
      pw.println(s"Converted ${conversion.inputType} to ${typeOf[OUT]}")
      conversion.toLines.map("  " + _).foreach(pw.println)
    }
  }

  /** Used to assert that there should be at most one item in the conversion and extract it into an Option.
    *
    * @param conversion the conversion from which the optional result will be extracted
    */

  def optional[OUT:TypeTag](conversion:PickaxeConversion[IN,OUT]) = {
    val out = conversion.items match {
      case Seq() => None
      case Seq(head) => Some(head)
      case items => throw new PickaxeCountException(conversion,s"found ${items.length} items where expecting at most one")
    }
    dump(conversion,out)
    out
  }

  /** Used to assert that there should be exactly one item in the conversion and extract it.
    *
    * @param conversion the conversion from which the single result will be extracted
    */

  def required[OUT:TypeTag](conversion:PickaxeConversion[IN,OUT]) = {
    val out = conversion.items match {
      case Seq(head) => head
      case items =>
        log.debug("CONVERSION: " + conversion)
        log.debug("ITEMS: " + conversion.items)
        throw new PickaxeCountException(conversion,s"found ${items.length} items where expecting exactly one:\n$conversion")
    }
    dump(conversion,out)
    out
  }

  /** Used to extract all of items from the conversion as a sequence.
    *
    * @param conversion the conversion from which the items will be extracted
    */

  def all[OUT:TypeTag](conversion:PickaxeConversion[IN,OUT]) = {
    val out = conversion.items
    dump(conversion,out)
    out
  }

  protected type Extractor[+OUT] = PartialFunction[IN,PickaxeConversion[IN,OUT]]

  /** Override for any recursive structure that can be broken down into multiple instances of the input
    * type and process recursively.
    */

  protected def extractRecursive[OUT:TypeTag](extract: => Extractor[OUT]):Extractor[OUT] = PartialFunction.empty

  /** Override for common (output-type-apathetic) conversions that can be performed for any output type
    *  Really, handles any input that can be treated the same regardless of its output type (as long as
    * the extractor can deal with it).  Use this for iterations and such that aren't really recursive
    * (based on the semantics of your particular pickaxe).  You can think of these as recursions that are
    * allowed even when recursions are disallowed.
    */

  protected def extractCommon[OUT:TypeTag](extract: => Extractor[OUT]):Extractor[OUT] = PartialFunction.empty

  protected def process[OUT:TypeTag](in:IN)(strict:Extractor[OUT],lax:Extractor[OUT] = PartialFunction.empty):PickaxeConversion[IN,OUT] = {
    def fail:Extractor[OUT] = {
      case in =>
        val actualType = in.getClass.getName
        val targetType = typeOf[OUT]
        val hint =
          if ( ! this.lax && lax.isDefinedAt(in) )
            ": allowing lax typing may make this conversion possible"
          else if ( ! this.recursive && extractRecursive(extract).isDefinedAt(in) )
            ": allowing recursion may make this conversion possible"
          else
            ""

        PickaxeConversion[IN,OUT](in,Left(Failure(new IllegalArgumentException(s"no conversion from '$in' ($actualType) to $targetType$hint"))))
    }

    def extract:Extractor[OUT] = (this.recursive,this.lax) match {
      case (true,false) => strict orElse extractRecursive(extract) orElse extractCommon(extract) orElse fail
      case (true,true) => strict orElse lax orElse extractRecursive(extract) orElse extractCommon(extract) orElse fail
      case (false,true) => strict orElse lax orElse extractCommon(extract) orElse fail
      case (false,false) => strict orElse extractCommon(extract) orElse fail
    }

    extract(in)
  }
}

/* pickaxe -- Copyright 2013 Justin Patterson -- All Rights Reserved */
