sbtPlugin := true

name := "sbt-cloudengine"

organization := "net.entrypass"

version := "0.2.1"

description := "sbt plugin for managing Google Cloud Engine resources"

licenses := Seq("BSD License" -> url("https://github.com/hanxue/sbt-cloudengine/blob/master/LICENSE"))

scalacOptions := Seq("-deprecation", "-unchecked")

publishArtifact in (Compile, packageBin) := true

publishArtifact in (Test, packageBin) := false

publishArtifact in (Compile, packageDoc) := false

publishArtifact in (Compile, packageSrc) := false

publishMavenStyle := false
