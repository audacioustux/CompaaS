package compaas.core

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior, DeathPactException, SupervisorStrategy}
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement
import akka.protobufv3.internal.compiler.PluginProtos.CodeGeneratorResponse.File
import org.graalvm.polyglot.Source

import java.nio.file.{Files, Path}
import scala.concurrent.Await
import scala.concurrent.duration.Duration

import concurrent.duration.DurationInt

object ComponentSystem:
  def apply(): Unit =
    given system: ActorSystem[ComponentManager.Message] = ActorSystem(ComponentManager(), "CompaaS")

    system ! ComponentManager.CreateComponent(
      Component(
        Language.Js,
        Source
          // .newBuilder("js", "export const echo = (msg) => msg;", "echo.js")
          .newBuilder(
            "js",
            Files.readString(
              Path.of("examples", "components", "greeter", "index.js")
            ),
            "greeter.js"
          )
          .mimeType("application/javascript+module")
          .build(),
        "echo",
        Some("echo component")
      )
    )

    AkkaManagement(system).start()
    // ClusterBootstrap(system).start()

    Await.result(system.whenTerminated, Duration.Inf)
