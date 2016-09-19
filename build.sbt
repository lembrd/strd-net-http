import sbt.Keys._

name := "strd-net-http"

organization := "org.strd"

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.8"
val nettyVersion = "4.1.0.Final"

val publishSettings = Seq(
  pomExtra := <scm>
    <url>https://github.com/lembrd/strd-net-http</url>
    <connection>git@github.com:lembrd/strd-net-http.git</connection>
  </scm>
    <developers>
      <developer>
        <id>lembrd</id>
        <name>Michael Shabunin</name>
        <url>https://github.com/lembrd</url>
      </developer>

      <developer>
        <id>bulay</id>
        <name>Alexander Bulay</name>
        <url>https://github.com/bulay</url>
      </developer>

      <developer>
        <id>chebba</id>
        <name>Kirill chEbba Chebunin</name>
        <url>https://github.com/chebba</url>
      </developer>
    </developers>,
  publishMavenStyle := true,
  publishArtifact in Test := false,
  bintrayPackageLabels := Seq("strd-net-http"),
  licenses += ("LGPL-3.0", url("https://opensource.org/licenses/lgpl-3.0.html"))
)

libraryDependencies ++= Seq(
  "io.netty" % "netty-codec-http" % nettyVersion,
  "io.netty" % "netty-common" % nettyVersion,
  "commons-lang" % "commons-lang" % "2.6",
  "org.slf4j" % "slf4j-api" % "1.7.21",
  "io.dropwizard.metrics" % "metrics-core" % "3.1.2"
)