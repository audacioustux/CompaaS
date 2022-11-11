val scala3Version            = "3.2.0"
val AkkaVersion              = "2.6.20"
val AkkaProjectionVersion    = "1.2.5"
val AkkaHttpVersion          = "10.2.10"
val GraalVersion             = "22.2.0"
val AkkaManagementVersion    = "1.1.4"
val AkkaPersistenceCassandra = "1.0.6"
val DatastaxJavaDriver       = "4.15.0"
val KafkaClientVersion       = "3.3.1"
val JsoniterScalaVersion     = "2.17.9"
val LogbackVersion           = "1.4.4"
val CatsVersion              = "2.8.0"

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
  .enablePlugins(JavaAppPackaging)
  .settings(
    name           := "monorepo-sbt",
    run / fork     := true,
    publish / skip := true,
    libraryDependencies ++= Seq(
      "com.typesafe.akka"             %% "akka-http"                    % AkkaHttpVersion,
      "com.typesafe.akka"             %% "akka-http-spray-json"         % AkkaHttpVersion,
      "com.typesafe.akka"             %% "akka-actor-typed"             % AkkaVersion,
      "com.typesafe.akka"             %% "akka-persistence-typed"       % AkkaVersion,
      "com.typesafe.akka"             %% "akka-stream-typed"            % AkkaVersion,
      "com.typesafe.akka"             %% "akka-cluster-typed"           % AkkaVersion,
      "com.typesafe.akka"             %% "akka-cluster-sharding-typed"  % AkkaVersion,
      "com.typesafe.akka"             %% "akka-persistence-query"       % AkkaVersion,
      "com.typesafe.akka"             %% "akka-discovery"               % AkkaVersion,
      "com.typesafe.akka"             %% "akka-discovery"               % AkkaVersion,
      "com.lightbend.akka"            %% "akka-projection-core"         % AkkaProjectionVersion,
      "com.typesafe.akka"             %% "akka-persistence-cassandra"   % AkkaPersistenceCassandra,
      "org.typelevel"                 %% "cats-core"                    % CatsVersion,
      "com.lightbend.akka.management" %% "akka-management"              % AkkaManagementVersion,
      "com.lightbend.akka.management" %% "akka-management-cluster-http" % AkkaManagementVersion,
      "com.lightbend.akka.management" %% "akka-management-cluster-bootstrap" % AkkaManagementVersion,
      // Test
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion     % Test,
      "com.typesafe.akka" %% "akka-http-testkit"        % AkkaHttpVersion % Test,
      "com.typesafe.akka" %% "akka-stream-testkit"      % AkkaVersion     % Test
    ).map(_.cross(CrossVersion.for3Use2_13)),
    libraryDependencies ++= Seq(
      "ch.qos.logback"                         % "logback-classic"     % LogbackVersion,
      "org.graalvm.sdk"                        % "graal-sdk"           % GraalVersion,
      "org.apache.kafka"                       % "kafka-clients"       % KafkaClientVersion,
      "com.datastax.oss"                       % "java-driver-core"    % DatastaxJavaDriver,
      "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core" % JsoniterScalaVersion,
      "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % JsoniterScalaVersion % "compile-internal",
      // Test
      "org.scalameta" %% "munit"     % "0.7.29" % Test,
      "org.scalatest" %% "scalatest" % "3.2.14" % Test
    ),
    nativeImageGraalHome := file(sys.env("GRAALVM_HOME")).toPath,
    // Disable documentation generation
    Compile / packageDoc / publishArtifact := false,
    Compile / doc / sources                := Seq()
  )

lazy val dev = taskKey[Unit]("Run a multi-node local cluster for development environment")
// NOTE: execute bin/dev directly for proper colors and formatting
dev := {
  import sys.process.*

  "bin/dev" !
}
