val scala3Version         = "3.2.0"
val AkkaVersion           = "2.6.20"
val AkkaProjectionVersion = "1.2.5"
val AkkaHttpVersion       = "10.2.10"
val GraalVersion          = "22.2.0"

inThisBuild(
  List(
    version                                        := "1.0",
    organization                                   := "com.audacioustux",
    scalaVersion                                   := scala3Version,
    scalafixOnCompile                              := true,
    semanticdbEnabled                              := true,
    semanticdbVersion                              := scalafixSemanticdb.revision,
    scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.6.0"
  )
)

Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val root = project
  .in(file("."))
  .enablePlugins(NativeImagePlugin)
  .settings(
    name           := "monorepo-sbt",
    run / fork     := true,
    publish / skip := true,
    libraryDependencies ++= Seq(
      "com.typesafe.akka"  %% "akka-actor-typed"            % AkkaVersion,
      "com.typesafe.akka"  %% "akka-persistence-typed"      % AkkaVersion,
      "com.typesafe.akka"  %% "akka-stream-typed"           % AkkaVersion,
      "com.typesafe.akka"  %% "akka-http"                   % AkkaHttpVersion,
      "com.typesafe.akka"  %% "akka-http-spray-json"        % AkkaHttpVersion,
      "com.typesafe.akka"  %% "akka-cluster-typed"          % AkkaVersion,
      "com.typesafe.akka"  %% "akka-cluster-sharding-typed" % AkkaVersion,
      "com.typesafe.akka"  %% "akka-persistence-query"      % AkkaVersion,
      "com.typesafe.akka"  %% "akka-discovery"              % AkkaVersion,
      "com.lightbend.akka" %% "akka-projection-core"        % AkkaProjectionVersion,
      // Test
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion     % Test,
      "com.typesafe.akka" %% "akka-http-testkit"        % AkkaHttpVersion % Test,
      "com.typesafe.akka" %% "akka-stream-testkit"      % AkkaVersion     % Test
    ).map(_.cross(CrossVersion.for3Use2_13)),
    libraryDependencies ++= Seq(
      "ch.qos.logback"  % "logback-classic" % "1.4.4",
      "org.scalameta"  %% "munit"           % "0.7.29" % Test,
      "org.scalatest"  %% "scalatest"       % "3.2.14" % Test,
      "org.graalvm.sdk" % "graal-sdk"       % GraalVersion
    ),
    nativeImageGraalHome := file(sys.env("GRAALVM_HOME")).toPath
  )

addCommandAlias("dev", "~reStart")
