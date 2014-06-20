package org.scalawag.pickaxe.sdom

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import org.scalawag.pickaxe.PickaxeConversionException
import org.scalawag.sdom._
import org.scalawag.pickaxe.PickaxeConversionException

class SdomPickaxeTest extends FunSuite with ShouldMatchers {

  private val xml = XML.parse("""
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
      <multi-level-attributes>
        <e id="id1">
          <e id="id3"/>
        </e>
        <e id="id2"/>
      </multi-level-attributes>
    </root>
  """).root

  test("convert - String - success") {
    import SdomPickaxe._
    mine[Seq[String]](xml \ "numbers" \ *) should be (Seq("1","2"))
  }

  test("convert - Int - success") {
    import SdomPickaxe._
    mine[Seq[Int]](xml \ "numbers" \ *) should be (Seq(1,2))
  }

  test("convert - Int - fail") {
    import SdomPickaxe._
    val ex = intercept[PickaxeConversionException[Iterable[Element],Int]] {
      mine[Seq[Int]](xml \ "booleans" \ *)
    }

    ex.getMessage should include ("NumberFormatException")
  }

  test("convert - Long - success") {
    import SdomPickaxe._
    mine[Seq[Long]](xml \ "numbers" \ *) should be (Seq(1L,2L))
  }

  test("convert - Long - fail") {
    import SdomPickaxe._
    val ex = intercept[PickaxeConversionException[Iterable[Element],Int]] {
      mine[Seq[Long]](xml \ "booleans" \ *)
    }

    ex.getMessage should include ("NumberFormatException")
  }

  test("convert - Float - success") {
    import SdomPickaxe._
    mine[Seq[Float]](xml \ "numbers" \ *) should be (Seq(1f,2f))
  }

  test("convert - Float - fail") {
    import SdomPickaxe._
    val ex = intercept[PickaxeConversionException[Iterable[Element],Int]] {
      mine[Seq[Float]](xml \ "booleans" \ *)
    }

    ex.getMessage should include ("NumberFormatException")
  }

  test("convert - Double - success") {
    import SdomPickaxe._
    mine[Seq[Double]](xml \ "numbers" \ *) should be (Seq(1.0,2.0))
  }

  test("convert - Double - fail") {
    import SdomPickaxe._
    val ex = intercept[PickaxeConversionException[Iterable[Element],Int]] {
      mine[Seq[Double]](xml \ "booleans" \ *)
    }

    ex.getMessage should include ("NumberFormatException")
  }

  test("convert - Boolean - success") {
    import SdomPickaxe._
    mine[Seq[Boolean]](xml \ "booleans" \ *) should be (Seq(true,false))
  }

  test("convert - Boolean - fail") {
    import SdomPickaxe._
    val ex = intercept[PickaxeConversionException[Iterable[Element],Int]] {
      mine[Seq[Boolean]](xml \ "numbers" \ *)
    }

    ex.getMessage should include ("IllegalArgumentException")
  }

  test("convert - Element - success") {
    import SdomPickaxe._
    mine[Seq[Element]](xml \ "numbers" \ *).map(_.spec) should be (Seq(
      ElementSpec("one",children = Iterable(TextSpec("1"))),
      ElementSpec("two",children = Iterable(TextSpec("2")))
    ))
  }

  test("convert - Element - fail") {
    import SdomPickaxe._
    val ex = intercept[PickaxeConversionException[Iterable[Element],Element]] {
      mine[Seq[Element]](xml \@ "id")
    }

    ex.getMessage should include ("no conversion")
  }

  test("direct - String - success") {
    import SdomPickaxe._
    all(string(xml \ "numbers" \ *)) should be (Seq("1","2"))
  }

  test("direct - Int - success") {
    import SdomPickaxe._
    all(int(xml \ "numbers" \ *)) should be (Seq(1,2))
  }

  test("direct - Int - fail") {
    import SdomPickaxe._
    val ex = intercept[PickaxeConversionException[Iterable[Element],Int]] {
      all(int(xml \ "booleans" \ *))
    }

    ex.getMessage should include ("NumberFormatException")
  }

  test("direct - Long - success") {
    import SdomPickaxe._
    all(long(xml \ "numbers" \ *)) should be (Seq(1L,2L))
  }

  test("direct - Long - fail") {
    import SdomPickaxe._
    val ex = intercept[PickaxeConversionException[Iterable[Element],Int]] {
      all(long(xml \ "booleans" \ *))
    }

    ex.getMessage should include ("NumberFormatException")
  }

  test("direct - Float - success") {
    import SdomPickaxe._
    all(float(xml \ "numbers" \ *)) should be (Seq(1f,2f))
  }

  test("direct - Float - fail") {
    import SdomPickaxe._
    val ex = intercept[PickaxeConversionException[Iterable[Element],Int]] {
      all(float(xml \ "booleans" \ *))
    }

    ex.getMessage should include ("NumberFormatException")
  }

  test("direct - Double - success") {
    import SdomPickaxe._
    all(double(xml \ "numbers" \ *)) should be (Seq(1.0,2.0))
  }

  test("direct - Double - fail") {
    import SdomPickaxe._
    val ex = intercept[PickaxeConversionException[Iterable[Element],Int]] {
      all(double(xml \ "booleans" \ *))
    }

    ex.getMessage should include ("NumberFormatException")
  }

  test("direct - Boolean - success") {
    import SdomPickaxe._
    all(boolean(xml \ "booleans" \ *)) should be (Seq(true,false))
  }

  test("direct - Boolean - fail") {
    import SdomPickaxe._
    val ex = intercept[PickaxeConversionException[Iterable[Element],Int]] {
      all(boolean(xml \ "numbers" \ *))
    }

    ex.getMessage should include ("IllegalArgumentException")
  }

  test("direct - Element - success") {
    import SdomPickaxe._
    val c = element(xml \ "numbers" \ *)
    all(c).map(_.spec) should be (Seq(
      ElementSpec("one",children = Iterable(TextSpec("1"))),
      ElementSpec("two",children = Iterable(TextSpec("2")))
    ))
  }

  test("direct - Element - fail") {
    import SdomPickaxe._
    intercept[PickaxeConversionException[Iterable[Element],Element]] {
      all(element(xml \@ "id"))
    }
  }

  test("use case - read from attribute") {
    import SdomPickaxe._
    mine[Int](xml \@ "id") should be (1234)
  }

  test("use case - read specific Element content") {
    import SdomPickaxe._
    mine[Int](xml \ "numbers" \ "one") should be (1)
  }

  test("use case - comments are ignored") {
    import SdomPickaxe._
    all(int(xml \ "comments" \ *)) should be (Seq(12,2))
  }

  test("use case - cdata is transparent") {
    import SdomPickaxe._
    required(int(xml \ "cdata" \ *)) should be (1234)
  }

  test("use case - empty Iterable[Element]") {
    import SdomPickaxe._
    all(int(xml \ "absent")) should be (Nil)
  }

  test("the whole thing fails if one item can't be converted") {
    val pickaxe = new SdomPickaxe(true)
    import pickaxe._
    val ex = intercept[PickaxeConversionException[Iterable[Element],Element]] {
      all(int(xml \ * \ *))
    }

    ex.getMessage should include ("NumberFormatException")
  }

  test("recursion fails if disallowed") {
    import SdomPickaxe._
    val ex = intercept[PickaxeConversionException[Iterable[Element],Element]] {
      all(int(xml \ "numbers"))
    }

    ex.getMessage should include ("no conversion")
  }

  test("recursion works only if allowed") {
    val pickaxe = new SdomPickaxe(true)
    import pickaxe._

    all(int(xml \ "numbers")) should be (Seq(1,2))
  }

  test("gathering attributes works") {
    import SdomPickaxe._
    val xml = XML.parse("""
      <multi-level-attributes>
        <e id="id1">
          <e id="id3"/>
        </e>
        <e id="id2"/>
      </multi-level-attributes>
    """)

    all(string(xml \\ "e" \@ "id")) should be (Seq("id1","id3","id2"))
  }
}

/* pickaxe -- Copyright 2013 Justin Patterson -- All Rights Reserved */
