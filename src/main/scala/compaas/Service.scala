package compaas

import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.http.scaladsl.server.Directives.*

object Service:
  def apply()(using ClusterSharding) = new Service()

class Service()(using ClusterSharding):

  def route = get:
    pathSingleSlash:
      complete("Hello World!")
