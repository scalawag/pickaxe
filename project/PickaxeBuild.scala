import sbt._
import Keys._
import de.johoop.jacoco4sbt._
import JacocoPlugin._

object PickaxeBuild extends Build {
  import Dependencies._

  val VERSION = "1.1-SNAPSHOT"

  val commonSettings =
    Defaults.defaultSettings ++ Seq(
      version := VERSION,
      crossPaths := false,
      scalacOptions ++= Seq("-unchecked","-deprecation","-feature","-language:implicitConversions","-target:jvm-1.6"),
      javaOptions ++= Seq("-Xmx256m","-XX:MaxPermSize=256m"),
      scalaVersion := "2.10.0",
//      testOptions += Tests.Argument("-oDF"),
      // Right now, the reflection stuff is not thread-safe so we have to execute our tests in sequence.
      // See: http://docs.scala-lang.org/overviews/reflection/thread-safety.html
      parallelExecution in Test := false,
      parallelExecution in jacoco.Config := false,
      libraryDependencies ++= Seq(scalatest,mockito),
      organization := "org.scalawag.pickaxe",
      resolvers += "sonatype-oss-snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
      publishMavenStyle := true,
      publishArtifact in Test := false,
      publishTo <<= version { (v: String) =>
        val nexus = "https://oss.sonatype.org/"
        if (v.trim.endsWith("SNAPSHOT"))
          Some("snapshots" at nexus + "content/repositories/snapshots")
        else
          Some("releases"  at nexus + "service/local/staging/deploy/maven2")
      },
      pomIncludeRepository := { _ => false },
      pomExtra :=
        <url>http://scalwag.org/timber</url>
        <licenses>
          <license>
            <name>Apache 2</name>
            <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
            <distribution>repo</distribution>
          </license>
        </licenses>
        <scm>
          <url>http://github.com/scalawag/timber</url>
          <connection>scm:git:git://github.com/scalawag/timber.git</connection>
        </scm>
        <developers>
          <developer>
            <id>justinp</id>
            <name>Justin Patterson</name>
            <email>justin@scalawag.org</email>
            <url>https://github.com/justinp</url>
          </developer>
        </developers>
  ) ++ jacoco.settings ++ Defaults.itSettings

  val pickaxe = Project("pickaxe",file("pickaxe"),
                        settings = commonSettings ++ Seq(
                          libraryDependencies ++= Seq(reflect,timberApi,timber)
                        )
                       )

  val pickaxeLiftJson = Project("pickaxe-lift-json",file("pickaxe-lift-json"),
                                settings = commonSettings ++ Seq(
                                  libraryDependencies ++= Seq(liftJson,timber)
                                )
                               ) dependsOn (pickaxe)

  val aggregator = Project("aggregate",file("."),
                           settings = commonSettings ++ Seq(
                             publish := {}
                           )) aggregate (pickaxe,pickaxeLiftJson)

  object Dependencies {
    val timberApi = "org.scalawag.timber" % "timber-api" % "0.3-SNAPSHOT" changing
    val reflect = "org.scala-lang" % "scala-reflect" % "2.10.0"
    val liftJson = "net.liftweb" %% "lift-json" % "2.5-RC1"

    val timber = "org.scalawag.timber" % "timber" % "0.3-SNAPSHOT" % "test" changing
    val scalatest = "org.scalatest" %% "scalatest" % "1.9" % "test"
    val mockito = "org.mockito" % "mockito-all" % "1.9.0" % "test"
  }
}

/* pickaxe -- Copyright 2012 Justin Patterson -- All Rights Reserved */
