name := "web2txt"

version := "1.0"

scalaVersion := "2.11.7"

resolvers += "spray repo" at "http://repo.spray.io"

libraryDependencies += "org.apache.pdfbox" % "pdfbox" % "1.8.9"

libraryDependencies += "org.bouncycastle" % "bcprov-jdk15on" % "1.52"

libraryDependencies += "com.typesafe.akka" % "akka-actor_2.11" % "2.3.12"

libraryDependencies += "io.spray" %% "spray-client" % "1.3.3"

libraryDependencies += "io.spray" %% "spray-routing" % "1.3.3"

libraryDependencies += "io.spray" %% "spray-httpx" % "1.3.3"

libraryDependencies += "org.slf4j" % "slf4j-log4j12" % "1.7.12"

libraryDependencies += "ch.qos.logback" % "logback-classic"  % "1.1.3"

mainClass in (Compile, run) := Some("web2txt.Main")