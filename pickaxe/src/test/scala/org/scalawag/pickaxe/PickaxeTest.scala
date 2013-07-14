package org.scalawag.pickaxe

import scala.reflect.runtime.universe._
import org.scalatest.{OneInstancePerTest,FunSuite}
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._

class PickaxeTest extends FunSuite with ShouldMatchers with MockitoSugar with OneInstancePerTest {
  private val conversion = mock[PickaxeConversion[String,Any]]
  when(conversion.toLines).thenReturn(Seq()) // prevent logging from crashing and burning

  private val pickaxe = new Pickaxe[String] {
    var convertArgs:(String,Type) = _

    override protected def convert = {
      case args =>
        convertArgs = args
        conversion
    }
  }

  test("arguments passed to 'convert'") {
    import pickaxe._

    when(conversion.items).thenReturn(Seq())

    mine[Seq[String]]("blarf")

    convertArgs should be (("blarf",typeOf[String]))
  }

  test("subclass doesn't handle requested type") {
    intercept[IllegalArgumentException] {
      (new Pickaxe[String]).mine[String]("-")
    }
  }

  test("target type not specified") {
    intercept[IllegalArgumentException] {
      (new Pickaxe).mine("-")
    }
  }

  test("mine[String] - zero") {
    import pickaxe._

    when(conversion.items).thenReturn(Seq())

    intercept[PickaxeCountException[String,String]] {
      mine[String]("-")
    }
  }

  test("mine[String] - one") {
    import pickaxe._

    when(conversion.items).thenReturn(Seq("a"))

    mine[String]("-") should be ("a")
  }

  test("mine[String] - two") {
    import pickaxe._

    when(conversion.items).thenReturn(Seq("a","b"))

    intercept[PickaxeCountException[String,String]] {
      mine[String]("-")
    }
  }

  test("mine[String] - failed conversion") {
    import pickaxe._

    when(conversion.items).thenThrow(new PickaxeConversionException(conversion))

    intercept[PickaxeConversionException[String,String]] {
      mine[String]("-")
    } should be (PickaxeConversionException(conversion))
  }

  test("mine[Option[String]] - zero") {
    import pickaxe._

    when(conversion.items).thenReturn(Seq())

    mine[Option[String]]("-") should be (None)
  }

  test("mine[Option[String]] - one") {
    import pickaxe._

    when(conversion.items).thenReturn(Seq("a"))

    mine[Option[String]]("-") should be (Some("a"))
  }

  test("mine[Option[String]] - two") {
    import pickaxe._

    when(conversion.items).thenReturn(Seq("a","b"))

    intercept[PickaxeCountException[String,Option[String]]] {
      mine[Option[String]]("-")
    }
  }

  test("mine[Option[String]] - failed conversion") {
    import pickaxe._

    when(conversion.items).thenThrow(new PickaxeConversionException(conversion))

    intercept[PickaxeConversionException[String,Option[String]]] {
      mine[Option[String]]("-")
    } should be (PickaxeConversionException(conversion))
  }

  test("mine[Seq[String]] - zero") {
    import pickaxe._

    when(conversion.items).thenReturn(Seq())

    mine[Seq[String]]("-") should be (Seq())
  }

  test("mine[Seq[String]] - one") {
    import pickaxe._

    when(conversion.items).thenReturn(Seq("a"))

    mine[Seq[String]]("-") should be (Seq("a"))
  }

  test("mine[Seq[String]] - two") {
    import pickaxe._

    when(conversion.items).thenReturn(Seq("a","b"))

    mine[Seq[String]]("-") should be (Seq("a","b"))
  }

  test("mine[Seq[String]] - failed conversion") {
    import pickaxe._

    when(conversion.items).thenThrow(new PickaxeConversionException(conversion))

    intercept[PickaxeConversionException[String,Seq[String]]] {
      mine[Seq[String]]("-")
    } should be (PickaxeConversionException(conversion))
  }

  test("required - zero") {
    import pickaxe._

    when(conversion.items).thenReturn(Seq())

    intercept[PickaxeCountException[String,String]] {
      required(conversion)
    }
  }

  test("required - one") {
    import pickaxe._

    when(conversion.items).thenReturn(Seq("a"))

    required(conversion) should be ("a")
  }

  test("required - two") {
    import pickaxe._

    when(conversion.items).thenReturn(Seq("a","b"))

    intercept[PickaxeCountException[String,String]] {
      required(conversion)
    }
  }

  test("required - failed conversion") {
    import pickaxe._

    when(conversion.items).thenThrow(new PickaxeConversionException(conversion))

    intercept[PickaxeConversionException[String,String]] {
      required(conversion)
    } should be (PickaxeConversionException(conversion))
  }

  test("optional - zero") {
    import pickaxe._

    when(conversion.items).thenReturn(Seq())

    optional(conversion) should be (None)
  }

  test("optional - one") {
    import pickaxe._

    when(conversion.items).thenReturn(Seq("a"))

    optional(conversion) should be (Some("a"))
  }

  test("optional - two") {
    import pickaxe._

    when(conversion.items).thenReturn(Seq("a","b"))

    intercept[PickaxeCountException[String,Option[String]]] {
      optional(conversion)
    }
  }

  test("optional - failed conversion") {
    import pickaxe._

    when(conversion.items).thenThrow(new PickaxeConversionException(conversion))

    intercept[PickaxeConversionException[String,Option[String]]] {
      optional(conversion)
    } should be (PickaxeConversionException(conversion))
  }

  test("all - zero") {
    import pickaxe._

    when(conversion.items).thenReturn(Seq())

    all(conversion) should be (Seq())
  }

  test("all - one") {
    import pickaxe._

    when(conversion.items).thenReturn(Seq("a"))

    all(conversion) should be (Seq("a"))
  }

  test("all - two") {
    import pickaxe._

    when(conversion.items).thenReturn(Seq("a","b"))

    all(conversion) should be (Seq("a","b"))
  }

  test("all - failed conversion") {
    import pickaxe._

    when(conversion.items).thenThrow(new PickaxeConversionException(conversion))

    intercept[PickaxeConversionException[String,Seq[String]]] {
      all(conversion)
    } should be (PickaxeConversionException(conversion))
  }

  test("process - strict supersedes all other conversions") {
    new ProcessorFixture {
      val pickaxe = new ProcessorPickaxe(false,false)
      import pickaxe._

      required(string("a")) should be ("strict - a")
    }
  }

  test("process - lax supersedes all but strict") {
    new ProcessorFixture {
      val pickaxe = new ProcessorPickaxe(false,true)
      import pickaxe._

      required(string("b")) should be ("lax - b")
    }
  }

  test("process - lax must be enabled to do anything") {
    new ProcessorFixture {
      val pickaxe = new ProcessorPickaxe(false,false)
      import pickaxe._

      required(string("b")) should be ("extractCommon - b")
    }
  }

  test("process - lax gives us a hint if disabled but possibly useful") {
    new ProcessorFixture {
      val pickaxe = new ProcessorPickaxe(false,false)
      import pickaxe._

      val ex = intercept[PickaxeConversionException[_,_]] {
        required(string("bb"))
      }

      ex.getMessage should include ("allowing lax typing may make this conversion possible")
    }
  }

  test("process - extractRecursive supersedes extractCommon") {
    new ProcessorFixture {
      val pickaxe = new ProcessorPickaxe(true,false)
      import pickaxe._

      required(string("c")) should be ("extractRecursive - c")
    }
  }

  test("process - extractRecursive must be enabled to do anything") {
    new ProcessorFixture {
      val pickaxe = new ProcessorPickaxe(false,false)
      import pickaxe._

      required(string("c")) should be ("extractCommon - c")
    }
  }

  test("process - extractRecursive gives us a hint if disabled but possibly useful") {
    new ProcessorFixture {
      val pickaxe = new ProcessorPickaxe(false,false)
      import pickaxe._

      val ex = intercept[PickaxeConversionException[_,_]] {
        required(string("cc"))
      }

      ex.getMessage should include ("allowing recursion may make this conversion possible")
    }
  }

  test("process - extractRecursive is actually recursive") {
    new ProcessorFixture {
      val pickaxe = new ProcessorPickaxe(true,true)
      import pickaxe._

      all(string("abcd")) should be (Seq("strict - a","lax - b","extractRecursive - c","extractCommon - d"))
    }
  }

  test("process - extractCommon is the last attempt") {
    new ProcessorFixture {
      val pickaxe = new ProcessorPickaxe(true,false)
      import pickaxe._

      required(string("d")) should be ("extractCommon - d")
    }
  }

  test("process - failing to find a case immediately") {
    new ProcessorFixture {
      val pickaxe = new ProcessorPickaxe(true,true)
      import pickaxe._

      val ex = intercept[PickaxeConversionException[_,_]] {
        required(string("z"))
      }

      // No hint
      ex.getMessage should not include ("allowing")
    }
  }

  test("process - failing to find a case recursively") {
    new ProcessorFixture {
      val pickaxe = new ProcessorPickaxe(true,true)
      import pickaxe._

      val ex = intercept[PickaxeConversionException[_,_]] {
        required(string("abcdz"))
      }

      // No hint
      ex.getMessage should not include ("allowing")
    }
  }

  trait ProcessorFixture {
    import PickaxeConversion._

    protected class ProcessorPickaxe(val recursive:Boolean,
                                   val lax:Boolean) extends Pickaxe[String](recursive,lax) {
      override protected def extractRecursive[OUT:TypeTag](extract: => Extractor[OUT]):Extractor[OUT] = {
        case x @ "a" => single(x,"extractRecursive - a".asInstanceOf[OUT])
        case x @ "b" => single(x,"extractRecursive - b".asInstanceOf[OUT])
        case x @ "c" => single(x,"extractRecursive - c".asInstanceOf[OUT])
        case x:String if x.length > 1 => descend(x,x.map(_.toString).map(extract))
      }

      /** Override for common (output-type-apathetic) conversions that can be performed for any output type
        * Really, handles any input that can be treated the same regardless of its output type (as long as
        * the extractor can deal with it).  Use this for iterations and such that aren't really recursive
        * (based on the semantics of your particular pickaxe).  You can think of these as recursions that are
        * allowed even when recursions are disallowed.
        */
      override protected def extractCommon[OUT:TypeTag](extract: => Extractor[OUT]):Extractor[OUT] = {
        case x @ "a" => single(x,"extractCommon - a".asInstanceOf[OUT])
        case x @ "b" => single(x,"extractCommon - b".asInstanceOf[OUT])
        case x @ "c" => single(x,"extractCommon - c".asInstanceOf[OUT])
        case x @ "d" => single(x,"extractCommon - d".asInstanceOf[OUT])
      }

      def string(in:String) = process(in)(
        strict = {
          case x @ "a" => single(x,"strict - a")
        },
        lax = {
          case x @ "a" => single(x,"lax - a")
          case x @ "b" => single(x,"lax - b")
          case x @ "bb" => single(x,"lax - bb")
        }
      )
    }

  }
}

/* pickaxe -- Copyright 2013 Justin Patterson -- All Rights Reserved */
