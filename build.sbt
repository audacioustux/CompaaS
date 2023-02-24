lazy val versions = new {
  val scala                    = "3.2.0"
  val Akka                     = "2.6.20"
  val AkkaProjection           = "1.2.5"
  val AkkaHttp                 = "10.2.10"
  val GraalSDK                 = "22.3.0"
  val AkkaManagement           = "1.1.4"
  val AkkaPersistenceCassandra = "1.0.6"
  val DatastaxJavaDriver       = "4.15.0"
  val KafkaClient              = "3.3.1"
  val JsoniterScala            = "2.21.2"
  val Logback                  = "1.4.4"
  val Cats                     = "2.8.0"
  val Munit                    = "0.7.29"
  val Scalatest                = "3.2.14"
}

inThisBuild(
  List(
    version                                        := "1.0",
    organization                                   := "com.audacioustux",
    scalaVersion                                   := versions.scala,
    scalafixOnCompile                              := true,
    semanticdbEnabled                              := true,
    semanticdbVersion                              := scalafixSemanticdb.revision,
    scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.6.0"
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
      "com.typesafe.akka"  %% "akka-http"                   % versions.AkkaHttp,
      "com.typesafe.akka"  %% "akka-http-spray-json"        % versions.AkkaHttp,
      "com.typesafe.akka"  %% "akka-actor-typed"            % versions.Akka,
      "com.typesafe.akka"  %% "akka-persistence-typed"      % versions.Akka,
      "com.typesafe.akka"  %% "akka-stream-typed"           % versions.Akka,
      "com.typesafe.akka"  %% "akka-cluster-typed"          % versions.Akka,
      "com.typesafe.akka"  %% "akka-cluster-sharding-typed" % versions.Akka,
      "com.typesafe.akka"  %% "akka-persistence-query"      % versions.Akka,
      "com.typesafe.akka"  %% "akka-discovery"              % versions.Akka,
      "com.typesafe.akka"  %% "akka-discovery"              % versions.Akka,
      "com.lightbend.akka" %% "akka-projection-core"        % versions.AkkaProjection,
      "com.typesafe.akka"  %% "akka-persistence-cassandra"  % versions.AkkaPersistenceCassandra,
      "org.typelevel"      %% "cats-core"                   % versions.Cats,
      "com.lightbend.akka.management" %% "akka-management"              % versions.AkkaManagement,
      "com.lightbend.akka.management" %% "akka-management-cluster-http" % versions.AkkaManagement,
      "com.lightbend.akka.management" %% "akka-management-cluster-bootstrap" % versions.AkkaManagement,
      // Test
      "com.typesafe.akka" %% "akka-http-testkit"        % versions.AkkaHttp % Test,
      "com.typesafe.akka" %% "akka-stream-testkit"      % versions.Akka     % Test,
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % versions.Akka     % Test,
      "com.typesafe.akka" %% "akka-persistence-testkit" % versions.Akka     % Test
    ).map(_.cross(CrossVersion.for3Use2_13)),
    libraryDependencies ++= Seq(
      "ch.qos.logback"   % "logback-classic"  % versions.Logback,
      "org.graalvm.sdk"  % "graal-sdk"        % versions.GraalSDK,
      "org.apache.kafka" % "kafka-clients"    % versions.KafkaClient,
      "com.datastax.oss" % "java-driver-core" % versions.DatastaxJavaDriver,
      "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core" % versions.JsoniterScala,
      "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % versions.JsoniterScala % "compile-internal",
      // Test
      "org.scalameta" %% "munit"     % versions.Munit     % Test,
      "org.scalatest" %% "scalatest" % versions.Scalatest % Test
    )
  )

lazy val projects: Seq[ProjectReference] = Seq(`compaas-core`)

lazy val root = project
  .in(file("."))
  .settings(name := "compaas")
  .aggregate(projects*)
  .dependsOn(projects.map(_ % "compile->compile")*)

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
  "-Xfatal-warnings",
  "-Xmigration"
)
