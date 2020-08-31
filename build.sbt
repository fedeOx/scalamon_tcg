name := "scalamon_tcg"

version := "0.1"

scalaVersion := "2.12.9"

lazy val root = (project in file("."))
  .settings(
    libraryDependencies ++= Seq(
      "junit" % "junit" % "4.12" % Test,
      "com.novocode" % "junit-interface" % "0.11" % Test,
      "org.scalatest" %% "scalatest" % "3.0.5" % Test,
    ),
    crossPaths := false, // https://github.com/sbt/junit-interface/issues/35
    Test / parallelExecution := false
  )