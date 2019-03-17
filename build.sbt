name := """casa-api"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)
parallelExecution in Test := false

scalaVersion := "2.12.6"

libraryDependencies += guice
libraryDependencies += "org.joda" % "joda-convert" % "1.9.2"
libraryDependencies += "net.logstash.logback" % "logstash-logback-encoder" % "4.11"
libraryDependencies += "com.netaporter" %% "scala-uri" % "0.4.16"
libraryDependencies += "net.codingwell" %% "scala-guice" % "4.2.1"
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.12"
libraryDependencies += "org.mockito" % "mockito-core" % "2.7.22" % Test
libraryDependencies += "org.tpolecat" %% "doobie-core" % "0.6.0"
libraryDependencies += "org.tpolecat" %% "doobie-postgres" % "0.6.0"
libraryDependencies += "org.tpolecat" %% "doobie-specs2" % "0.6.0" % Test
libraryDependencies += "org.tpolecat" %% "doobie-scalatest" % "0.6.0" % Test
