import _root_.sbt.Keys._
import _root_.sbt._

lazy val defaultSettings = Seq(
  scalaVersion := "2.11.6",
  version := "1.1",
  organization := "viper",
  libraryDependencies ++= Seq(
    "net.java.dev.glazedlists" % "glazedlists_java15" % "1.9.0" withSources(),
    "com.kitfox.svg" % "svg-salamander" % "1.0",
    "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test" withSources(),
    "org.codehaus.woodstox" % "woodstox-core-asl" % "4.2.0" exclude("javax.xml.stream", "stax-api") withSources()
  )
)

scalacOptions in ThisBuild ++= Seq(
  "-unchecked",
  "-deprecation",
  "-feature"
)

//javacOptions ++= Seq("-target", "1.5")

mainClass in (Compile, run) := Some("viper.Viper")

mainClass in (Compile, packageBin) := Some("viper.Viper")

exportJars := true

lazy val domain = project
  .settings(defaultSettings: _*)

lazy val `domain-log` = project
  .settings(defaultSettings: _*)
  .dependsOn(domain)

lazy val `domain-message` = project
  .settings(defaultSettings: _*)
  .dependsOn(domain)

lazy val util = project
  .settings(defaultSettings: _*)

lazy val source = project
  .settings(defaultSettings: _*)
  .dependsOn(domain)

lazy val `source-log` = project
  .settings(defaultSettings: _*)
  .dependsOn(source, `domain-log`, util)

lazy val store = project
  .settings(defaultSettings: _*)

lazy val `store-mapdb` = project
  .settings(defaultSettings: _*)
  .dependsOn(store)

lazy val ui = project
  .settings(defaultSettings: _*)
  .dependsOn(domain, source, util)

lazy val main = project
  .settings(defaultSettings: _*)
  .dependsOn(`domain-log`, `domain-message`, `source-log`, ui)

lazy val root = project.in(file("."))
  .settings(defaultSettings: _*)
  .aggregate(domain, `domain-log`, `domain-message`, source, `source-log`, util, ui, main)
  .dependsOn(domain, `domain-log`, `domain-message`, source, `source-log`, util, ui, main)
