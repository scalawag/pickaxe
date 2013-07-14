package org.scalawag.pickaxe

case class PickaxeCountException[IN,OUT](conversion:PickaxeConversion[IN,OUT],message:String) extends Exception {
  override lazy val getMessage = s"$message: $conversion"
}

/* pickaxe -- Copyright 2013 Justin Patterson -- All Rights Reserved */
