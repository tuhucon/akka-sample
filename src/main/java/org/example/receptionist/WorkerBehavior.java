package org.example.receptionist;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.receptionist.ServiceKey;

public class WorkerBehavior extends AbstractBehavior<WorkerBehavior.Command> {

    public interface Command {}

    public static class XCommand implements Command {}

    public static class YCommand implements Command {}

    public static ServiceKey<Command> workerServiceKey = ServiceKey.create(Command.class, "worker");

    public static Behavior<Command> create() {
        return Behaviors.setup(WorkerBehavior::new);
    }
    public WorkerBehavior(ActorContext<Command> context) {
        super(context);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(XCommand.class, this::onXCommand)
                .onMessage(YCommand.class, this::onYCommand)
                .build();
    }

    private Behavior<Command> onXCommand(XCommand cmd) {
        getContext().getLog().info("XCommand from " + this);
        return Behaviors.same();
    }

    private Behavior<Command> onYCommand(YCommand cmd) {
        getContext().getLog().info("YCommand from " + this);
        return Behaviors.same();
    }
}
