import sbt.*
import sbt.Keys.*

object Dependencies {

  object Akka {

    private object Groups {
      val typesafe   = "com.typesafe.akka"
      val lightbend  = "com.lightbend.akka"
      val management = "com.lightbend.akka.management"
      val discovery  = "com.lightbend.akka.discovery"
    }

    private object Versions {
      val actors           = "2.8.4"
      val http             = "10.5.2"
      val projection       = "1.4.2"
      val management       = "1.4.1"
      val persistenceR2dbc = "1.1.1"
    }

    object Compile {
      // actors
      val actorsTyped = Groups.typesafe %% "akka-actor-typed"  % Versions.actors
      val streamTyped = Groups.typesafe %% "akka-stream-typed" % Versions.actors
      // persistence
      val persistence      = Groups.typesafe  %% "akka-persistence-typed" % Versions.actors
      val persistenceQuery = Groups.typesafe  %% "akka-persistence-query" % Versions.actors
      val persistenceR2dbc = Groups.lightbend %% "akka-persistence-r2dbc" % Versions.persistenceR2dbc
      // projection
      val projection = Groups.lightbend %% "akka-projection-core" % Versions.projection
      // cluster
      val clusterTyped    = Groups.typesafe %% "akka-cluster-typed"          % Versions.actors
      val clusterSharding = Groups.typesafe %% "akka-cluster-sharding-typed" % Versions.actors
      val discovery       = Groups.typesafe %% "akka-discovery"              % Versions.actors
      val clusterTools    = Groups.typesafe %% "akka-cluster-tools"          % Versions.actors
      // management
      val management              = Groups.management %% "akka-management"                   % Versions.management
      val clusterHttp             = Groups.management %% "akka-management-cluster-http"      % Versions.management
      val clusterBootstrap        = Groups.management %% "akka-management-cluster-bootstrap" % Versions.management
      val discoveryKubernetesApi  = Groups.discovery  %% "akka-discovery-kubernetes-api"     % Versions.management
      val rollingUpdateKubernetes = Groups.management %% "akka-rolling-update-kubernetes"    % Versions.management
      // http
      val http = Groups.typesafe %% "akka-http" % Versions.http
      // serialization
      val httpSprayJson        = Groups.typesafe %% "akka-http-spray-json"       % Versions.http
      val serializationJackson = Groups.typesafe %% "akka-serialization-jackson" % Versions.actors
    }

    object Test {
      val httpTestkit        = Groups.typesafe %% "akka-http-testkit"        % Versions.http   % "test"
      val actorTestkitTyped  = Groups.typesafe %% "akka-actor-testkit-typed" % Versions.actors % "test"
      val persistenceTestkit = Groups.typesafe %% "akka-persistence-testkit" % Versions.actors % "test"
    }

  }

  object JsoniterScala {
    private val version = "2.23.4"
    private val group   = "com.github.plokhotnyuk.jsoniter-scala"

    val jsoniter = group %% "jsoniter-scala-core"   % version
    val macros   = group %% "jsoniter-scala-macros" % version % "compile-internal"
  }

  object Compile {

    private object Versions {
      val opentelemetry = "1.29.0"
      val logback       = "1.4.11"
      val yugabyte      = "4.15.0-yb-1"
      val cats          = "2.10.0"
    }

    val opentelemetry  = "io.opentelemetry" % "opentelemetry-api" % Versions.opentelemetry
    val logback        = "ch.qos.logback"   % "logback-classic"   % Versions.logback
    val yugabyteDriver = "com.yugabyte"     % "java-driver-core"  % Versions.yugabyte
    val cats           = "org.typelevel"   %% "cats-core"         % Versions.cats
  }

  object Test {

    private object Versions {
      val munit     = "0.7.29"
      val scalatest = "3.2.16"
    }

    val munit     = "org.scalameta" %% "munit"     % Versions.munit     % "test"
    val scalatest = "org.scalatest" %% "scalatest" % Versions.scalatest % "test"
  }

  lazy val compaas =
    libraryDependencies ++=
      Seq(
        Akka.Compile.actorsTyped,
        Akka.Compile.streamTyped,
        Akka.Compile.persistence,
        Akka.Compile.persistenceQuery,
        Akka.Compile.persistenceR2dbc,
        Akka.Compile.projection,
        Akka.Compile.clusterTyped,
        Akka.Compile.clusterSharding,
        Akka.Compile.discovery,
        Akka.Compile.clusterTools,
        Akka.Compile.management,
        Akka.Compile.clusterHttp,
        Akka.Compile.clusterBootstrap,
        Akka.Compile.discoveryKubernetesApi,
        Akka.Compile.rollingUpdateKubernetes,
        Akka.Compile.http,
        Akka.Compile.httpSprayJson,
        Akka.Compile.serializationJackson,
        JsoniterScala.jsoniter,
        JsoniterScala.macros,
        Compile.opentelemetry,
        Compile.logback,
        Compile.yugabyteDriver,
        Compile.cats,
        Test.munit,
        Test.scalatest,
      )

}
