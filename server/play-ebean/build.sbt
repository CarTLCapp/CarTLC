name := "play-ebean"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.11.11"

libraryDependencies += jdbc
libraryDependencies += "com.adrianhurt" %% "play-bootstrap" % "1.0-P25-B3"

libraryDependencies += javaJdbc
libraryDependencies += cache
libraryDependencies += javaWs

libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.21"

libraryDependencies += evolutions

lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayEbean)
  