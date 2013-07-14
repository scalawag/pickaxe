package org.scalawag.pickaxe

import scala.reflect.runtime.universe._
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import net.liftweb.json.JsonParser
import net.liftweb.json.JsonAST._
import scala.util.{Failure,Try,Success}

class LiftJsonPickaxeTest extends FunSuite with ShouldMatchers {

  private val json = JsonParser.parse("""
    {
      "true": true,
      "false": false,
      "int": 8,
      "double": 7.4,
      "string": "name",
      "null": null,
      "stringnum": "42",
      "stringtrue": "true",
      "stringfalse": "false",
      "one": 1,
      "zero": 0,
      "object": {
        "a": 1
      },
      "array": [
        11,
        22,
        33
      ],
      "complexity": [
        {
          "name": "alice",
          "value": 1,
          "values": [2,4,6,8]
        },
        {
          "name": "bob",
          "value": 2,
          "values": []
        },
        {
          "name": "charlie",
          "value": {
            "name": "david",
            "value": "h",
            "values": [2.4,2.6,2.8]
          },
          "values": [10,12,14]
        }
      ]
    ]
  """)

  val strict = ("strict",new LiftJsonPickaxe(false,false))
  val lax = ("lax",new LiftJsonPickaxe(false,true))
  val recursive = ("recursive",new LiftJsonPickaxe(true,false))
  val recursiveLax = ("lax,recursive",new LiftJsonPickaxe(true,true))

  val jstring = json \ "string"
  val jstringnum = json \ "stringnum"
  val jint = json \ "int"
  val jdouble = json \ "double"
  val jtrue = json \ "true"
  val jfalse = json \ "false"
  val jnull = json \ "null"
  val jnothing = json \ "nothing"
  val jzero = json \ "zero"
  val jstringtrue = json \ "stringtrue"
  val jstringfalse = json \ "stringfalse"
  val jobject = json \ "object"
  val jarray = json \ "array"

  val int = ("int",(pickaxe:LiftJsonPickaxe,jvalue:JValue) => pickaxe.int(jvalue))
  val long = ("long",(pickaxe:LiftJsonPickaxe,jvalue:JValue) => pickaxe.long(jvalue))
  val float = ("float",(pickaxe:LiftJsonPickaxe,jvalue:JValue) => pickaxe.float(jvalue))
  val double = ("double",(pickaxe:LiftJsonPickaxe,jvalue:JValue) => pickaxe.double(jvalue))
  val boolean = ("boolean",(pickaxe:LiftJsonPickaxe,jvalue:JValue) => pickaxe.boolean(jvalue))
  val string = ("string",(pickaxe:LiftJsonPickaxe,jvalue:JValue) => pickaxe.string(jvalue))
  val obj = ("obj",(pickaxe:LiftJsonPickaxe,jvalue:JValue) => pickaxe.jobject(jvalue))
  val arr = ("arr",(pickaxe:LiftJsonPickaxe,jvalue:JValue) => pickaxe.jarray(jvalue))

  // These just keep the columns nice since "true" and "false" are different lengths.

  val T = true
  val F = false

  def passing[OUT:TypeTag](pickaxe:(String,LiftJsonPickaxe),
                           conversion:(String,(LiftJsonPickaxe,JValue) => PickaxeConversion[JValue,OUT]),
                           jvalue:JValue,
                           expected:Seq[OUT],
                           desc:String = null) {
    val description = Option(desc).getOrElse(s"${jvalue} =${pickaxe._1}=> ${conversion._1}")

    test(s"$description (direct)") {
      pickaxe._2.all(conversion._2(pickaxe._2,jvalue)) should be (expected)
    }

    test(s"$description (mine)") {
      pickaxe._2.mine[Seq[OUT]](jvalue) should be (expected)
    }
  }

  def failing[OUT:TypeTag](pickaxe:(String,LiftJsonPickaxe),
                           conversion:(String,(LiftJsonPickaxe,JValue) => PickaxeConversion[JValue,OUT]),
                           jvalue:JValue,
                           desc:String = null) {
    val description = Option(desc).getOrElse(s"${jvalue} =${pickaxe._1}=> ${conversion._1}")

    test(s"$description (direct)") {
      intercept[PickaxeConversionException[_,_]](pickaxe._2.all(conversion._2(pickaxe._2,jvalue)))
    }

    test(s"$description (mine)") {
      intercept[PickaxeConversionException[_,_]](pickaxe._2.mine[Seq[OUT]](jvalue))
    }
  }

  def tests[OUT:TypeTag](conversion:(String,(LiftJsonPickaxe,JValue) => PickaxeConversion[JValue,OUT]),
                      validForStrict:Boolean,
                      validForLax:Boolean,
                      jvalue:JValue,
                      expected:Seq[OUT] = Seq()) {

    if ( validForStrict ) {
      passing[OUT](strict,conversion,jvalue,expected)
    } else {
      failing[OUT](strict,conversion,jvalue)
    }

    if ( validForLax ) {
      passing[OUT](lax,conversion,jvalue,expected)
    } else {
      failing[OUT](lax,conversion,jvalue)
    }

  }

  // The type has to be explicit here because the compiler is inferring java.lang.String instead of scala.String
  tests[String](string,F,T,jint,Seq("8"))
  tests[String](string,F,T,jdouble,Seq("7.4"))
  tests[String](string,F,T,jtrue,Seq("true"))
  tests[String](string,F,T,jfalse,Seq("false"))
  tests[String](string,F,T,jnull,Seq("null"))
  tests[String](string,T,T,jstring,Seq("name"))
  tests[String](string,T,T,jnothing,Seq())
  tests[String](string,F,F,jobject)
  tests[String](string,F,F,jarray)

  tests(int,T,T,jint,Seq(8))
  tests(int,F,T,jdouble,Seq(7))
  tests(int,F,T,jtrue,Seq(1))
  tests(int,F,T,jfalse,Seq(0))
  tests(int,F,T,jnull,Seq(0))
  tests(int,F,T,jstringnum,Seq(42))
  tests(int,F,F,jstring)
  tests(int,T,T,jnothing,Seq())
  tests(int,F,F,jobject)
  tests(int,F,F,jarray)

  tests(long,T,T,jint,Seq(8L))
  tests(long,F,T,jdouble,Seq(7L))
  tests(long,F,T,jtrue,Seq(1L))
  tests(long,F,T,jfalse,Seq(0L))
  tests(long,F,T,jnull,Seq(0L))
  tests(long,F,T,jstringnum,Seq(42L))
  tests(long,F,F,jstring)
  tests(long,T,T,jnothing,Seq())
  tests(long,F,F,jobject)
  tests(long,F,F,jarray)

  tests(double,F,T,jint,Seq(8.0))
  tests(double,T,T,jdouble,Seq(7.4))
  tests(double,F,T,jtrue,Seq(1.0))
  tests(double,F,T,jfalse,Seq(0.0))
  tests(double,F,T,jnull,Seq(0.0))
  tests(double,F,T,jstringnum,Seq(42.0))
  tests(double,F,F,jstring)
  tests(double,T,T,jnothing,Seq())
  tests(double,F,F,jobject)
  tests(double,F,F,jarray)

  tests(float,F,T,jint,Seq(8f))
  tests(float,T,T,jdouble,Seq(7.4f))
  tests(float,F,T,jtrue,Seq(1f))
  tests(float,F,T,jfalse,Seq(0f))
  tests(float,F,T,jnull,Seq(0f))
  tests(float,F,T,jstringnum,Seq(42f))
  tests(float,F,F,jstring)
  tests(float,T,T,jnothing,Seq())
  tests(float,F,F,jobject)
  tests(float,F,F,jarray)

  tests(boolean,F,T,jint,Seq(true))
  tests(boolean,F,T,jzero,Seq(false))
  tests(boolean,F,T,jdouble,Seq(true))
  tests(boolean,T,T,jtrue,Seq(true))
  tests(boolean,T,T,jfalse,Seq(false))
  tests(boolean,F,T,jnull,Seq(false))
  tests(boolean,F,F,jstringnum)
  tests(boolean,F,F,jstring)
  tests(boolean,F,T,jstringtrue,Seq(true))
  tests(boolean,F,T,jstringfalse,Seq(false))
  tests(boolean,T,T,jnothing,Seq())
  tests(boolean,F,F,jobject)
  tests(boolean,F,F,jarray)

  tests(obj,F,F,jint)
  tests(obj,F,F,jdouble)
  tests(obj,F,F,jtrue)
  tests(obj,F,F,jnull)
  tests(obj,F,F,jstring)
  tests(obj,T,T,jnothing,Seq())
  tests(obj,T,T,jobject,Seq(JObject(List(JField("a",JInt(1))))))
  tests(obj,F,F,jarray)

  tests(arr,F,F,jint)
  tests(arr,F,F,jdouble)
  tests(arr,F,F,jtrue)
  tests(arr,F,F,jnull)
  tests(arr,F,F,jstring)
  tests(arr,T,T,jnothing,Seq())
  tests(arr,F,F,jobject)
  tests(arr,T,T,jarray,Seq(JArray(List(JInt(11),JInt(22),JInt(33)))))

  // Some recursive use case tests

  passing(recursive,int,json \ "complexity" \ "values",Seq(2,4,6,8,10,12,14),"use case - mine query results")
  passing(recursiveLax,float,json \\ "values",Seq(2f,4f,6f,8f,2.4f,2.6f,2.8f,10f,12f,14f),"use case - mine recursive query results")
  failing(recursive,float,json \\ "values","use case - a single bad conversion spoils the lot")
}

/* pickaxe -- Copyright 2013 Justin Patterson -- All Rights Reserved */
