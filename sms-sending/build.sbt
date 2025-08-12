name := "sms-sending"

organization := "org.ps"

version := "1.0"

scalaVersion := "2.13.12"

libraryDependencies ++= Seq(
  "com.vonage" % "client" % "9.3.1",
  "com.typesafe" % "config" % "1.4.4"
)
