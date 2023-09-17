package compaas.utils.streams

import akka.actor.typed.{ ActorRef, Behavior }
import akka.actor.typed.Terminated
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.AskPattern.*
import akka.actor.typed.scaladsl.Behaviors
import akka.stream.Materializer
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{ Flow, Keep, Sink, Source }
import akka.stream.typed.scaladsl.{ ActorSink, ActorSource }

object ActorFlow:

  sealed private trait EndOfStream
  private case object Success extends EndOfStream
  private case object Failure extends EndOfStream

  def apply[In, Out](
      behavior: ActorRef[Out] => Behavior[In],
      actorName: String,
      context: ActorContext[?],
      bufferSize: Int = 16,
      overflowStrategy: OverflowStrategy = OverflowStrategy.dropHead,
  )(implicit materializer: Materializer): Flow[In, Out, ?] =
    val (outActor, publisher) = ActorSource
      .actorRef[Out](
        {
          case _ if false =>
        }: PartialFunction[Out, Unit],
        {
          case e: Any if false =>
            new Exception(e.toString)
        }: PartialFunction[Any, Throwable],
        bufferSize,
        overflowStrategy,
      )
      .toMat(Sink.asPublisher(false))(Keep.both)
      .run()

    val sink = Flow[In]
      .map(Right[EndOfStream, In])
      .to(
        ActorSink.actorRef[Either[EndOfStream, In]](
          context.spawn(
            Behaviors.setup[Either[EndOfStream, In]] { context =>
              val flowActor = context.spawn(behavior(outActor), "flowActor")
              context.watch(flowActor)

              Behaviors
                .receiveMessage[Either[EndOfStream, In]] {
                  case Right(in) =>
                    flowActor ! in
                    Behaviors.same
                  case Left(_) =>
                    context.stop(flowActor)
                    Behaviors.same
                }
                .receiveSignal { case (_, Terminated(_)) =>
                  Behaviors.stopped
                }
            },
            actorName,
          ),
          Left(Success),
          _ => Left(Failure),
        )
      )

    Flow.fromSinkAndSource(sink, Source.fromPublisher(publisher))

  end apply

end ActorFlow
