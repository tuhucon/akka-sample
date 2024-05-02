package org.example.textparser;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.List;
import java.util.UUID;

public class MainBehavior extends AbstractBehavior<List<String>> {

    public static Behavior<List<String>> create() {
        return Behaviors.setup(MainBehavior::new);
    }

    private ActorRef<ManagerBehavior.Command> manager;

    public MainBehavior(ActorContext<List<String>> context) {
        super(context);
        manager = context.spawn(ManagerBehavior.create(), "manager");
    }

    @Override
    public Receive<List<String>> createReceive() {
        return newReceiveBuilder()
                .onMessage(List.class, this::parseContent)
                .build();
    }

    private Behavior<List<String>> parseContent(List<String> content) {
        ManagerBehavior.ParseCommand cmd = new ManagerBehavior.ParseCommand(UUID.randomUUID(), content);
        manager.tell(cmd);
        return this;
    }
}
