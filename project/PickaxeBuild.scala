import sbt._
import Keys._
import de.johoop.jacoco4sbt._
import JacocoPlugin._
import com.typesafe.sbt.osgi.SbtOsgi._
import OsgiKeys._
import org.scalawag.sbt.gitflow.GitFlow

object PickaxeBuild extends Build {
  import Dependencies._

  val VERSION = GitFlow.WorkingDir.version.toString

  val commonSettings =
    Defaults.defaultSettings ++ osgiSettings ++ Seq(
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
      libraryDependencies ++= Seq(scalatest,mockito,timber),
      organization := "org.scalawag.pickaxe",
      resolvers += "sonatype-oss-releases" at "https://oss.sonatype.org/content/repositories/releases/",
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

  val pickaxe =
    Project("pickaxe",file("pickaxe"),
      settings = commonSettings ++ Seq(
        libraryDependencies ++= Seq(reflect,timberApi),
        exportPackage ++= Seq(
          "org.scalawag.pickaxe"
        ),
        importPackage ++= Seq(
          "org.scalawag.timber.backend;version=0.4"
        )
      )
     )

  val pickaxeSdom =
    Project("pickaxe-sdom",file("pickaxe-sdom"),
      settings = commonSettings ++ Seq(
        libraryDependencies ++= Seq(sdom),
        exportPackage ++= Seq(
          "org.scalawag.pickaxe.sdom"
        )
      )
     ) dependsOn (pickaxe)

  val pickaxeLiftJson =
    Project("pickaxe-lift-json",file("pickaxe-lift-json"),
      settings = commonSettings ++ Seq(
        libraryDependencies ++= Seq(liftJson),
        exportPackage ++= Seq(
          "org.scalawag.pickaxe.json"
        )
      )
     ) dependsOn (pickaxe)

  val aggregator = Project("aggregate",file("."),
                           settings = Defaults.defaultSettings ++ Seq(
                             publishTo := Some("unused" at "unused"),
                             packagedArtifacts := Map.empty
                           )) aggregate (pickaxe,pickaxeLiftJson,pickaxeSdom)

  object Dependencies {
    val sdom = "org.scalawag.sdom" % "sdom" % "0.1.0"
    val timberApi = "org.scalawag.timber" % "timber-api" % "0.4.0"
    val reflect = "org.scala-lang" % "scala-reflect" % "2.10.0"
    val liftJson = "net.liftweb" %% "lift-json" % "2.5-RC1"

    val timber = "org.scalawag.timber" % "timber" % "0.4.0" % "test"
    val scalatest = "org.scalatest" %% "scalatest" % "1.9" % "test"
    val mockito = "org.mockito" % "mockito-all" % "1.9.0" % "test"
  }
}

/* pickaxe -- Copyright 2012 Justin Patterson -- All Rights Reserved */
