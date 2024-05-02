package org.example.pingpong;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class PongBehavior extends AbstractBehavior<PongBehavior.PongMessage> {

    public static class PongMessage {

        ActorRef<PingBehavior.PingMessage> pingActor;

        public PongMessage(ActorRef<PingBehavior.PingMessage> pingActor) {
            this.pingActor = pingActor;
        }

        public String getMessage() {
            return "PONG";
        }
    }

    public static Behavior<PongMessage> create(Integer maxMessageCount) {
        return Behaviors.setup(context -> new PongBehavior(context, maxMessageCount));
    }

    private final Integer maxMessageCount;
    private Integer count = 0;

    private PongBehavior(ActorContext<PongMessage> context, Integer maxMessageCount) {
        super(context);
        if (maxMessageCount <= 0) {
            throw new RuntimeException("maxMessageCount must be greater or equal than 0 but init with: " + maxMessageCount);
        }
        this.maxMessageCount = maxMessageCount;
    }

    @Override
    public Receive<PongMessage> createReceive() {
        return newReceiveBuilder()
                .onMessage(PongMessage.class, this::onMessage)
                .build();
    }

    public Behavior<PongMessage> onMessage(PongMessage message) {
        getContext().getLog().info("Message: " + message.getMessage() + " from: " + message.pingActor.toString());
//        System.out.println("Message: " + message.getMessage() + " from: " + message.pingActor.toString());
        message.pingActor.tell(new PingBehavior.PingMessage(getContext().getSelf()));
        count++;
        if (count == maxMessageCount) {
            return Behaviors.stopped();
        }
        return this;
    }



}
