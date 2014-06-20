package org.scalawag.pickaxe.sdom

import org.scalatest.FunSuite
import scala.concurrent.{ExecutionContext, future, Future, Await}
import scala.concurrent.duration.Duration
import org.scalawag.sdom._
import org.scalatest.matchers.ShouldMatchers
import ExecutionContext.Implicits.global
import org.scalawag.pickaxe.sdom.SdomPickaxe._

class MultithreadTest extends FunSuite with ShouldMatchers {

  test("1000 parses at a time") {
    val ints = Stream.from(0).take(1000)
    val xmls = ints.map( n => s"<a><b><c>$n</c></b></a>")
    val futures = xmls map { xml =>
      future {
        val doc = XML.parse(xml)
        required(int( doc \\ "b" \ * ))
      }
    }
    Await.result(Future.sequence(futures),Duration.Inf) should be (ints)
  }
}

/* sdom -- Copyright 2014 Justin Patterson -- All Rights Reserved */
