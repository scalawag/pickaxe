package org.scalawag.pickaxe

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import scala.xml.{NodeSeq,Elem}

class XmlPickaxeTest extends FunSuite with ShouldMatchers {

  private val xml =
    <root id="1234">
      <booleans>
        <one>true</one>
        <two>false</two>
      </booleans>
      <numbers>
        <one>1</one>
        <two>2</two>
      </numbers>
      <comments>
        <one>1<!-- this is not really here -->2</one>
        <two>
          <!-- nothing to see here -->
          2
        </two>
      </comments>
      <cdata>
        <one>1<![CDATA[23]]>4</one>
      </cdata>
    </root>

  test("convert - String - success") {
    import XmlPickaxe._
    mine[Seq[String]](xml \ "numbers" \ "_") should be (Seq("1","2"))
  }

  test("convert - Int - success") {
    import XmlPickaxe._
    mine[Seq[Int]](xml \ "numbers" \ "_") should be (Seq(1,2))
  }

  test("convert - Int - fail") {
    import XmlPickaxe._
    val ex = intercept[PickaxeConversionException[NodeSeq,Int]] {
      mine[Seq[Int]](xml \ "booleans" \ "_")
    }

    ex.getMessage should include ("NumberFormatException")
  }

  test("convert - Long - success") {
    import XmlPickaxe._
    mine[Seq[Long]](xml \ "numbers" \ "_") should be (Seq(1L,2L))
  }

  test("convert - Long - fail") {
    import XmlPickaxe._
    val ex = intercept[PickaxeConversionException[NodeSeq,Int]] {
      mine[Seq[Long]](xml \ "booleans" \ "_")
    }

    ex.getMessage should include ("NumberFormatException")
  }

  test("convert - Float - success") {
    import XmlPickaxe._
    mine[Seq[Float]](xml \ "numbers" \ "_") should be (Seq(1f,2f))
  }

  test("convert - Float - fail") {
    import XmlPickaxe._
    val ex = intercept[PickaxeConversionException[NodeSeq,Int]] {
      mine[Seq[Float]](xml \ "booleans" \ "_")
    }

    ex.getMessage should include ("NumberFormatException")
  }

  test("convert - Double - success") {
    import XmlPickaxe._
    mine[Seq[Double]](xml \ "numbers" \ "_") should be (Seq(1.0,2.0))
  }

  test("convert - Double - fail") {
    import XmlPickaxe._
    val ex = intercept[PickaxeConversionException[NodeSeq,Int]] {
      mine[Seq[Double]](xml \ "booleans" \ "_")
    }

    ex.getMessage should include ("NumberFormatException")
  }

  test("convert - Boolean - success") {
    import XmlPickaxe._
    mine[Seq[Boolean]](xml \ "booleans" \ "_") should be (Seq(true,false))
  }

  test("convert - Boolean - fail") {
    import XmlPickaxe._
    val ex = intercept[PickaxeConversionException[NodeSeq,Int]] {
      mine[Seq[Boolean]](xml \ "numbers" \ "_")
    }

    ex.getMessage should include ("IllegalArgumentException")
  }

  test("convert - Elem - success") {
    import XmlPickaxe._
    mine[Seq[Elem]](xml \ "numbers" \ "_") should be (Seq(<one>1</one> , <two>2</two>))
  }

  test("convert - Elem - fail") {
    import XmlPickaxe._
    val ex = intercept[PickaxeConversionException[NodeSeq,Elem]] {
      mine[Seq[Elem]](xml \ "@id")
    }

    ex.getMessage should include ("no conversion")
  }

  test("direct - String - success") {
    import XmlPickaxe._
    all(string(xml \ "numbers" \ "_")) should be (Seq("1","2"))
  }

  test("direct - Int - success") {
    import XmlPickaxe._
    all(int(xml \ "numbers" \ "_")) should be (Seq(1,2))
  }

  test("direct - Int - fail") {
    import XmlPickaxe._
    val ex = intercept[PickaxeConversionException[NodeSeq,Int]] {
      all(int(xml \ "booleans" \ "_"))
    }

    ex.getMessage should include ("NumberFormatException")
  }

  test("direct - Long - success") {
    import XmlPickaxe._
    all(long(xml \ "numbers" \ "_")) should be (Seq(1L,2L))
  }

  test("direct - Long - fail") {
    import XmlPickaxe._
    val ex = intercept[PickaxeConversionException[NodeSeq,Int]] {
      all(long(xml \ "booleans" \ "_"))
    }

    ex.getMessage should include ("NumberFormatException")
  }

  test("direct - Float - success") {
    import XmlPickaxe._
    all(float(xml \ "numbers" \ "_")) should be (Seq(1f,2f))
  }

  test("direct - Float - fail") {
    import XmlPickaxe._
    val ex = intercept[PickaxeConversionException[NodeSeq,Int]] {
      all(float(xml \ "booleans" \ "_"))
    }

    ex.getMessage should include ("NumberFormatException")
  }

  test("direct - Double - success") {
    import XmlPickaxe._
    all(double(xml \ "numbers" \ "_")) should be (Seq(1.0,2.0))
  }

  test("direct - Double - fail") {
    import XmlPickaxe._
    val ex = intercept[PickaxeConversionException[NodeSeq,Int]] {
      all(double(xml \ "booleans" \ "_"))
    }

    ex.getMessage should include ("NumberFormatException")
  }

  test("direct - Boolean - success") {
    import XmlPickaxe._
    all(boolean(xml \ "booleans" \ "_")) should be (Seq(true,false))
  }

  test("direct - Boolean - fail") {
    import XmlPickaxe._
    val ex = intercept[PickaxeConversionException[NodeSeq,Int]] {
      all(boolean(xml \ "numbers" \ "_"))
    }

    ex.getMessage should include ("IllegalArgumentException")
  }

  test("direct - Elem - success") {
    import XmlPickaxe._
    val c = elem(xml \ "numbers" \ "_")
    all(c) should be (Seq(<one>1</one> , <two>2</two>))
  }

  test("direct - Elem - fail") {
    import XmlPickaxe._
    intercept[PickaxeConversionException[NodeSeq,Elem]] {
      all(elem(xml \ "@id"))
    }
  }

  test("use case - read from attribute") {
    import XmlPickaxe._
    mine[Int](xml \ "@id") should be (1234)
  }

  test("use case - read specific element content") {
    import XmlPickaxe._
    mine[Int](xml \ "numbers" \ "one") should be (1)
  }

  test("use case - comments are ignored") {
    import XmlPickaxe._
    all(int(xml \ "comments" \ "_")) should be (Seq(12,2))
  }

  test("use case - cdata is transparent") {
    import XmlPickaxe._
    required(int(xml \ "cdata" \ "_")) should be (1234)
  }

  test("use case - empty NodeSeq") {
    import XmlPickaxe._
    all(int(xml \ "absent")) should be (Nil)
  }

  test("the whole thing fails if one item can't be converted") {
    import XmlPickaxe._
    val ex = intercept[PickaxeConversionException[NodeSeq,Elem]] {
      all(int(xml \ "_" \ "_"))
    }

    ex.getMessage should include ("NumberFormatException")
  }

  test("recursion fails if disallowed") {
    import XmlPickaxe._
    val ex = intercept[PickaxeConversionException[NodeSeq,Elem]] {
      all(int(xml \ "numbers"))
    }

    ex.getMessage should include ("no conversion")
  }

  test("recursion works only if allowed") {
    val pickaxe = new XmlPickaxe(true)
    import pickaxe._

    all(int(xml \ "numbers")) should be (Seq(1,2))
  }

}

/* pickaxe -- Copyright 2013 Justin Patterson -- All Rights Reserved */
