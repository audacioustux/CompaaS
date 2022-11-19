package compaas.bloom

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ComponentAgentSpec extends AnyWordSpec with BeforeAndAfterAll with Matchers {
  val testKit = ActorTestKit()
  import testKit.*

  "Component" when {
    "source is valid" should {
      "parse" in {
        val component = Component("js", "console.log('hello world')", "main")
        val agent     = spawn(ComponentAgent(component))
        val probe     = createTestProbe[ComponentAgent.ParseEvent]()
        agent ! ComponentAgent.Parse(probe.ref)
        probe.expectMessage(ComponentAgent.Parsed)
      }
    }
    "source is invalid" should {
      "send ParseFailed(reason)" in {
        val component = Component("js", "console.log 'hello world')", "main")
        val agent     = spawn(ComponentAgent(component))
        val probe     = createTestProbe[ComponentAgent.ParseEvent]()

        agent ! ComponentAgent.Parse(probe.ref)
        val err = probe.expectMessageType[ComponentAgent.ParseFailed]

        err.reason.isSyntaxError shouldBe true
        err.reason.getMessage should include("SyntaxError: main:1:13")
      }
    }
  }

  override def afterAll(): Unit = testKit.shutdownTestKit()
}
