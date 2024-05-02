package org.example.pingpong;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class MainBehavior extends AbstractBehavior<MainBehavior.MainMessage> {

    public static class MainMessage {

    }

    public static Behavior<MainMessage> create() {
        return Behaviors.setup(MainBehavior::new);
    }

    private ActorRef<PingBehavior.PingMessage> pingActor;

    public MainBehavior(ActorContext<MainMessage> context) {
        super(context);
        pingActor = getContext().spawn(PingBehavior.create(), "pingActor");
    }

    @Override
    public Receive<MainMessage> createReceive() {
        return newReceiveBuilder()
                .onMessage(MainMessage.class, this::onMessage)
                .build();
    }

    private Behavior<MainMessage> onMessage(MainMessage message) {
        ActorRef<PongBehavior.PongMessage> pongActor = getContext().spawn(PongBehavior.create(3), "pongActor");
        pingActor.tell(new PingBehavior.PingMessage(pongActor));
        return this;
    }


}
