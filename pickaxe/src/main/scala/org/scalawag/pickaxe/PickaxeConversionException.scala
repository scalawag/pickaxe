package org.scalawag.pickaxe

// This extends RuntimeException so that it can more easily be used with Mockito in the tests.

case class PickaxeConversionException[IN,OUT](conversion:PickaxeConversion[IN,OUT]) extends RuntimeException {
  override def getMessage = s"failed to convert input to target type\n$conversion"
}

/* pickaxe -- Copyright 2013 Justin Patterson -- All Rights Reserved */
