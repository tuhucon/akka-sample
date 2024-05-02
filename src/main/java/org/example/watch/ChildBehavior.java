package org.example.watch;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class ChildBehavior extends AbstractBehavior<ChildBehavior.Command> {

    public interface Command {

    }

    public static Behavior<Command> create() {
        return Behaviors.setup(ChildBehavior::new);
    }

    public ChildBehavior(ActorContext<Command> context) {
        super(context);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onAnyMessage(cmd -> {
                    getContext().getLog().info("Child get a command: " + cmd.toString());
                    throw new RuntimeException("Child always throw exception for all message");
                })
                .build();
    }
}
