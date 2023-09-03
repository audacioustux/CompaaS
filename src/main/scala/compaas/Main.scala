package compaas

import akka.actor.typed.ActorSystem

@main
def run = ActorSystem(CompaaS(), "compaas")
