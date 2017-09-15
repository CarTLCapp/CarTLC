name := "play"

version := "2.0.0-SNAPSHOT"

scalaVersion := "2.11.11"

libraryDependencies += jdbc
libraryDependencies += "com.adrianhurt" %% "play-bootstrap" % "1.0-P25-B3"

libraryDependencies += javaJdbc
libraryDependencies += cache
libraryDependencies += javaWs

libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.21"

libraryDependencies += evolutions

lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-java-sdk-core" % "1.11.136",
  "com.amazonaws" % "aws-java-sdk-s3" % "1.11.136"
)

javacOptions ++= Seq(
  "-Xlint:deprecation",
  "-Xlint:unchecked"
)
