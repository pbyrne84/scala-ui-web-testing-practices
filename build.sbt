lazy val baseName = "scala-ui-web-testing-practices"

val scala213Version = "2.13.10"

scalaVersion := scala213Version

//not to be used in ci, intellij has got a bit bumpy in the format on save on optimize imports across the project
val formatAndTest =
  taskKey[Unit](
    "format all code then run tests, do not use on CI as any changes will not be committed"
  )


scalaVersion := scala213Version
scalacOptions ++= Seq(
  "-encoding",
  "utf8",
  "-feature",
  "-language:implicitConversions",
  "-language:existentials",
  "-unchecked"
) ++
  (CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, 13)) => Seq("-Ytasty-reader") // flags only needed in Scala 2
    case Some((3, _)) => Seq("-no-indent") // flags only needed in Scala 3
    case _ => Seq.empty
  })
formatAndTest := {
  (Test / test)
    .dependsOn(Compile / scalafmtAll)
    .dependsOn(Test / scalafmtAll)
}.value

Test / test := (Test / test)
  .dependsOn(Compile / scalafmtCheck)
  .dependsOn(Test / scalafmtCheck)
  .value


lazy val PekkoVersion = "1.0.1"

name := baseName
enablePlugins(SbtTwirl)


libraryDependencies ++= Vector(
  "ch.qos.logback" % "logback-core" % "1.4.12",
  "org.slf4j" % "slf4j-api" % "2.0.12",
  "org.flywaydb" % "flyway-core" % "9.16.2",
  "org.postgresql" % "postgresql" % "42.5.6",
  "com.github.tminglei" %% "slick-pg" % "0.22.1",
  "com.h2database" % "h2" % "2.1.214",
  "org.apache.pekko" %% "pekko-stream" % PekkoVersion,
  "org.apache.pekko" %% "pekko-stream-testkit" % PekkoVersion % Test,
  "org.apache.pekko" %% "pekko-actor-typed" % PekkoVersion,
  "org.apache.pekko" %% "pekko-actor-testkit-typed" % PekkoVersion % Test,
  "org.apache.pekko" %% "pekko-http-testkit" % PekkoVersion % Test,
  "org.apache.pekko" %% "pekko-http" % PekkoVersion,
  "com.typesafe.play" %% "twirl-api" % "1.6.5",
  "com.typesafe.slick" %% "slick" % "3.5.1",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.5.1",
  "org.jsoup" % "jsoup" % "1.18.1",
  "org.slf4j" % "slf4j-nop" % "1.7.26",
  "org.seleniumhq.selenium" % "selenium-chrome-driver" % "4.19.1",
  "org.seleniumhq.selenium" % "selenium-support" % "4.19.1",
  "io.cucumber" %% "cucumber-scala" % "8.21.0",
  "org.scalatest" %% "scalatest" % "3.2.18" % Test,
  "org.scalamock" %% "scalamock" % "5.2.0" % Test
)
