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
  val JsoniterScala            = "2.17.9"
  val Logback                  = "1.4.4"
  val Cats                     = "2.8.0"
  val munit                    = "0.7.29"
  val scalatest                = "3.2.14"
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

Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val root = project
  .in(file("."))
  .enablePlugins(NativeImagePlugin)
  .enablePlugins(JavaAppPackaging)
  .settings(
    name           := "compaas",
    run / fork     := true,
    publish / skip := true,
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
      "org.scalameta" %% "munit"     % versions.munit     % Test,
      "org.scalatest" %% "scalatest" % versions.scalatest % Test
    ),
    nativeImageGraalHome := file(sys.env("GRAALVM_HOME")).toPath,
    // Disable documentation generation
    Compile / packageDoc / publishArtifact := false,
    Compile / doc / sources                := Seq()
  )

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
