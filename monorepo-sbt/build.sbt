val scala3Version = "3.2.0"
val AkkaVersion = "2.6.20"
val AkkaProjectionVersion = "1.2.5"
val AkkaHttpVersion = "10.2.10"
val GraalVersion = "22.2.0"

lazy val root = project
  .in(file("."))
  .enablePlugins(NativeImagePlugin)
  .settings(
    name := "monorepo-sbt",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % Test,
      "com.typesafe.akka" %% "akka-persistence-typed" % AkkaVersion,
      "com.typesafe.akka" %% "akka-stream-typed" % AkkaVersion,
      "com.typesafe.akka" %% "akka-stream-testkit" % AkkaVersion,
      "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-testkit" % AkkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,
      "com.typesafe.akka" %% "akka-cluster-typed" % AkkaVersion,
      "com.typesafe.akka" %% "akka-cluster-sharding-typed" % AkkaVersion,
      "com.typesafe.akka" %% "akka-persistence-query" % AkkaVersion,
      "com.typesafe.akka" %% "akka-discovery" % AkkaVersion,
      "com.lightbend.akka" %% "akka-projection-core" % AkkaProjectionVersion
    ).map(_.cross(CrossVersion.for3Use2_13)),
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % "1.2.11",
      "org.scalameta" %% "munit" % "0.7.29" % Test,
      "org.graalvm.sdk" % "graal-sdk" % "22.2.0"
    ),
    Compile / run / fork := true,
    nativeImageGraalHome := file(sys.env("GRAALVM_HOME")).toPath,
    inThisBuild(
      List(
        scalaVersion := scala3Version,
        semanticdbEnabled := true,
        semanticdbVersion := scalafixSemanticdb.revision
      )
    ),
    ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.6.0"
  )
