lazy val baseName = "scala-ui-web-testing-practices"

val scala3Version = "3.3.6"

//not to be used in ci, intellij has got a bit bumpy in the format on save on optimize imports across the project
val formatAndTest =
  taskKey[Unit](
    "format all code then run tests, do not use on CI as any changes will not be committed"
  )

scalaVersion := scala3Version
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
    case Some((3, _))  => Seq("-no-indent")     // flags only needed in Scala 3
    case _             => Seq.empty
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
  "ch.qos.logback"          % "logback-core"               % "1.4.12",
  "org.slf4j"               % "slf4j-api"                  % "2.0.17",
  "org.flywaydb"            % "flyway-core"                % "11.11.2",
  "org.postgresql"          % "postgresql"                 % "42.7.7",
  "com.github.tminglei"    %% "slick-pg"                   % "0.23.1",
  "org.flywaydb"            % "flyway-database-postgresql" % "11.11.2",
  "io.github.iltotore"     %% "iron"                       % "3.1.0",
  "org.apache.pekko"       %% "pekko-stream"               % PekkoVersion,
  "org.apache.pekko"       %% "pekko-stream-testkit"       % PekkoVersion % Test,
  "org.apache.pekko"       %% "pekko-actor-typed"          % PekkoVersion,
  "org.apache.pekko"       %% "pekko-actor-testkit-typed"  % PekkoVersion % Test,
  "org.apache.pekko"       %% "pekko-http-testkit"         % PekkoVersion % Test,
  "org.apache.pekko"       %% "pekko-http"                 % PekkoVersion,
  "com.typesafe.play"      %% "twirl-api"                  % "1.6.10",
  "com.typesafe.slick"     %% "slick"                      % "3.6.1",
  "com.typesafe.slick"     %% "slick-hikaricp"             % "3.6.1",
  "org.jsoup"               % "jsoup"                      % "1.21.1",
  "org.slf4j"               % "slf4j-nop"                  % "2.0.17",
  "org.seleniumhq.selenium" % "selenium-chrome-driver"     % "4.35.0",
  "org.seleniumhq.selenium" % "selenium-support"           % "4.35.0",
  "io.cucumber"            %% "cucumber-scala"             % "8.31.1",
  "org.scalatest"          %% "scalatest"                  % "3.2.19"     % Test,
  "org.scalamock"          %% "scalamock"                  % "7.4.1"      % Test
)
