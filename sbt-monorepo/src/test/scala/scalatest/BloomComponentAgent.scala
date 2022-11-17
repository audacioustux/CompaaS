package scalatest

import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class BloomComponentAgent extends AnyWordSpec with Matchers with ScalatestRouteTest {}
