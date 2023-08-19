lazy val versions = new {
  val scala                = "3.3.0"
  val Akka                 = "2.8.4"
  val AkkaProjection       = "1.4.2"
  val AkkaHttp             = "10.5.2"
  val GraalSDK             = "23.0.1"
  val AkkaManagement       = "1.4.1"
  val AkkaPersistenceR2dbc = "1.1.1"
  val JsoniterScala        = "2.23.2"
  val Logback              = "1.4.11"
  val Cats                 = "2.10.0"
  val Munit                = "0.7.29"
  val Scalatest            = "3.2.16"
  val OpenTelemetry        = "1.29.0"
  val YugabyteDB           = "4.15.0-yb-1"
} // updated: 8 / 19 / 23

inThisBuild(
  List(
    organization        := "com.audacioustux",
    scalaVersion        := versions.scala,
    scalafixOnCompile   := true,
    semanticdbEnabled   := true,
    run / fork          := true,
    Global / cancelable := false,
    scalacOptions ++= Seq(
      "-explain",
      "-indent",
      "-rewrite",
      "-print-lines",
      "-deprecation",
      "-explain-types",
      "-feature",
      "-unchecked",
      "-Ykind-projector",
      "-Wunused:all",
      "-Xfatal-warnings",
      "-Xmigration"
    )
  )
)

lazy val `compaas-bench` = project
  .in(file("compaas-bench"))
  .enablePlugins(JmhPlugin)
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor-typed" % versions.Akka
    ).map(_.cross(CrossVersion.for3Use2_13)),
    libraryDependencies ++= Seq(
      "ch.qos.logback"  % "logback-classic" % versions.Logback,
      "org.graalvm.sdk" % "graal-sdk"       % versions.GraalSDK
    ),
    Compile / resourceGenerators += Def.task {
      import sys.process.*
      "cargo build --release" !

      val wasmDir = (Compile / resourceManaged).value / "wasm"
      val wasmFiles: Seq[File] =
        (root.base / "target" / "wasm32-unknown-unknown" / "release").listFiles
          .filter(_.getName.endsWith(".wasm"))

      wasmFiles.map { wasmFile =>
        val targetFile = wasmDir / wasmFile.getName
        IO.copyFile(wasmFile, targetFile)
        s"wasm-strip ${targetFile}".!

        targetFile
      }
    }.taskValue
  )

lazy val `compaas-core` = project
  .in(file("compaas-core"))
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka"             %% "akka-http"                         % versions.AkkaHttp,
      "com.typesafe.akka"             %% "akka-http-spray-json"              % versions.AkkaHttp,
      "com.typesafe.akka"             %% "akka-actor-typed"                  % versions.Akka,
      "com.typesafe.akka"             %% "akka-persistence-typed"            % versions.Akka,
      "com.typesafe.akka"             %% "akka-stream-typed"                 % versions.Akka,
      "com.typesafe.akka"             %% "akka-cluster-typed"                % versions.Akka,
      "com.typesafe.akka"             %% "akka-cluster-sharding-typed"       % versions.Akka,
      "com.typesafe.akka"             %% "akka-persistence-query"            % versions.Akka,
      "com.typesafe.akka"             %% "akka-discovery"                    % versions.Akka,
      "com.lightbend.akka"            %% "akka-projection-core"              % versions.AkkaProjection,
      "com.lightbend.akka.management" %% "akka-management"                   % versions.AkkaManagement,
      "com.lightbend.akka"            %% "akka-persistence-r2dbc"            % versions.AkkaPersistenceR2dbc,
      "com.lightbend.akka.management" %% "akka-management-cluster-http"      % versions.AkkaManagement,
      "com.lightbend.akka.management" %% "akka-management-cluster-bootstrap" % versions.AkkaManagement,
      "com.lightbend.akka.discovery"  %% "akka-discovery-kubernetes-api"     % versions.AkkaManagement,
      "com.lightbend.akka.management" %% "akka-rolling-update-kubernetes"    % versions.AkkaManagement,
      "com.typesafe.akka"             %% "akka-cluster-tools"                % versions.Akka,
      "com.typesafe.akka"             %% "akka-serialization-jackson"        % versions.Akka,
      "com.typesafe.akka"             %% "akka-http-testkit"                 % versions.AkkaHttp % Test,
      "com.typesafe.akka"             %% "akka-actor-testkit-typed"          % versions.Akka     % Test,
      "com.typesafe.akka"             %% "akka-persistence-testkit"          % versions.Akka     % Test
    ).map(_.cross(CrossVersion.for3Use2_13)),
    libraryDependencies ++= Seq(
      "io.opentelemetry"                       % "opentelemetry-api"     % versions.OpenTelemetry,
      "ch.qos.logback"                         % "logback-classic"       % versions.Logback,
      "org.graalvm.sdk"                        % "graal-sdk"             % versions.GraalSDK,
      "org.typelevel"                         %% "cats-core"             % versions.Cats,
      "com.yugabyte"                           % "java-driver-core"      % versions.YugabyteDB,
      "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core"   % versions.JsoniterScala,
      "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % versions.JsoniterScala % "compile-internal",
      // Test
      "org.scalameta" %% "munit"     % versions.Munit     % Test,
      "org.scalatest" %% "scalatest" % versions.Scalatest % Test
    ),
    excludeDependencies ++= Seq(ExclusionRule("com.datastax.oss", "java-driver-core"))
  )

lazy val projects: Seq[ProjectReference] = Seq(`compaas-core`)

lazy val root = project
  .in(file("."))
  .settings(name := "compaas")
  .aggregate(projects*)
  .dependsOn(projects.map(_ % "compile->compile")*)
  .enablePlugins(JavaAppPackaging)
