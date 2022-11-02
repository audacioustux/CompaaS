import akka.actor.typed.ActorSystem
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity}
import akka.persistence.typed.PersistenceId
import akka.util.Timeout

import scala.concurrent.Future

import concurrent.duration.DurationInt

class HelloWorldService(system: ActorSystem[?]) {
  import system.executionContext

  private val sharding = ClusterSharding(system)

  // registration at startup
  sharding.init(Entity(typeKey = HelloWorld.TypeKey) { entityContext =>
    HelloWorld(
      entityContext.entityId,
      PersistenceId(entityContext.entityTypeKey.name, entityContext.entityId)
    )
  })

  private implicit val askTimeout: Timeout = Timeout(5.seconds)

  def greet(worldId: String, whom: String): Future[Int] = {
    val entityRef = sharding.entityRefFor(HelloWorld.TypeKey, worldId)
    val greeting  = entityRef ? HelloWorld.Greet(whom)
    greeting.map(_.numberOfPeople)
  }

}
