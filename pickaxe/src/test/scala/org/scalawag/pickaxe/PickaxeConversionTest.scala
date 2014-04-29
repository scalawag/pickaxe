package org.scalawag.pickaxe

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

class PickaxeConversionTest extends FunSuite with ShouldMatchers {
  import PickaxeConversion._

  test("items from Right") {
    val pc = descend("-",Seq(single("1","a"),sequence("2",Seq("b","c"))))
    pc.items should be (Seq("a","b","c"))
  }

  test("nothing from Left(Success)") {
    val pc = sequence("1",Seq())
    pc.items should be (Seq())
  }

  test("items from Left(Success)") {
    val pc = sequence("1",Seq("a","b"))
    pc.items should be (Seq("a","b"))
  }

  test("items from Left(Failure)") {
    val t = new Throwable
    val pc = failure("1",t)

    val ex = intercept[PickaxeConversionException[_,_]](pc.items)

    ex.conversion should be (pc)
  }

  test("items from Right(Left(Failure))") {
    val t = new Throwable
    val pc = descend("-",Seq(single("1","a"),failure("2",t)))

    val ex = intercept[PickaxeConversionException[_,_]](pc.items)

    ex.conversion should be (pc)
  }

}

/* pickaxe -- Copyright 2013 Justin Patterson -- All Rights Reserved */
