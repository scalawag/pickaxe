// pickaxe -- Copyright 2013 Justin Patterson -- All Rights Reserved

scalacOptions := Seq("-feature","-deprecation")

addSbtPlugin("de.johoop" % "jacoco4sbt" % "2.1.1")

addSbtPlugin("com.typesafe.sbt" % "sbt-pgp" % "0.8")

addSbtPlugin("com.typesafe.sbt" % "sbt-osgi" % "0.7.0")

resolvers += "sonatype-oss-releases" at "http://oss.sonatype.org/content/repositories/releases/"

libraryDependencies += "org.scalawag.sbt.gitflow" % "sbt-gitflow" % "1.1.0"
