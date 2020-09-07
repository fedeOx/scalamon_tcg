name := "scalamon_tcg"

version := "0.1"

scalaVersion := "2.12.9"
// Determine OS version of JavaFX binaries
lazy val osName = System.getProperty("os.name") match {
  case n if n.startsWith("Linux")   => "linux"
  case n if n.startsWith("Mac")     => "mac"
  case n if n.startsWith("Windows") => "win"
  case _ => throw new Exception("Unknown platform!")
}


lazy val root = (project in file("."))
  .settings(
    libraryDependencies ++= Seq(
      "org.scalafx" %% "scalafx" % "12.0.1-R17",
      "junit" % "junit" % "4.12" % Test,
      "com.novocode" % "junit-interface" % "0.11" % Test,
      "io.github.typhon0" % "AnimateFX" % "1.2.1",
      "org.scalatest" %% "scalatest" % "3.1.1" % "test",
      "org.openjfx" % s"javafx-base" % "12.0.1" classifier osName,
      "org.openjfx" % s"javafx-controls" % "12.0.1" classifier osName,
      "org.openjfx" % s"javafx-fxml" % "12.0.1" classifier osName,
      "org.openjfx" % s"javafx-graphics" % "12.0.1" classifier osName,
      "org.openjfx" % s"javafx-media" % "12.0.1" classifier osName,
      "org.openjfx" % s"javafx-swing" % "12.0.1" classifier osName,
      "org.openjfx" % s"javafx-web" % "12.0.1" classifier osName,
      "io.circe" %% "circe-core" % "0.13.0",
      "io.circe" %% "circe-generic" % "0.13.0",
      "io.circe" %% "circe-parser" % "0.13.0",
      "io.circe" %% "circe-optics" % "0.13.0",
      "io.iteratee" %% "iteratee-files" % "0.19.0"
    ),
    crossPaths := false, // https://github.com/sbt/junit-interface/issues/35
    Test / parallelExecution := false
  )

coverageEnabled := true

// Add dependency on JavaFX libraries, OS dependent
lazy val javaFXModules = Seq("base", "controls", "fxml", "graphics", "media", "swing", "web")
libraryDependencies ++= javaFXModules.map( m =>
  "org.openjfx" % s"javafx-$m" % "14.0.1" classifier osName
)
