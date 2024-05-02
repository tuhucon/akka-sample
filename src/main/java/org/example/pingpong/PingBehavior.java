package org.example.pingpong;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class PingBehavior extends AbstractBehavior<PingBehavior.PingMessage> {

    public static class PingMessage {
        ActorRef<PongBehavior.PongMessage> pongActor;

        public PingMessage(ActorRef<PongBehavior.PongMessage> pongActor) {
            this.pongActor = pongActor;
        }

        public String getMessage() {
            return "PING";
        }
    }

    public static Behavior<PingMessage> create() {
        return Behaviors.setup(PingBehavior::new);
    }

    public PingBehavior(ActorContext<PingMessage> context) {
        super(context);
    }

    @Override
    public Receive<PingMessage> createReceive() {
        return newReceiveBuilder()
                .onMessage(PingMessage.class, this::onMessage)
                .build();
    }

    private Behavior<PingMessage> onMessage(PingMessage message) {
        getContext().getLog().info("Message: " + message.getMessage() + " from actor: " + message.pongActor.toString());
//        System.out.println("Message: " + message.getMessage() + " from actor: " + message.pongActor.toString());
        message.pongActor.tell(new PongBehavior.PongMessage(getContext().getSelf()));
        return this;
    }

}
