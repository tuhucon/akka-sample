package org.example.router;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.receptionist.ServiceKey;

public class WorkerBehavior extends AbstractBehavior<WorkerBehavior.Command> {

    public interface Command {}

    public static class ExceptionCommand implements Command {}

    public static Behavior<Command> create() {
        return Behaviors.setup(WorkerBehavior::new);
    }

    public WorkerBehavior(ActorContext<Command> context) {
        super(context);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(ExceptionCommand.class, this::onExceptionCommand)
                .build();
    }

    private Behavior<Command> onExceptionCommand(ExceptionCommand cmd) {
        throw new RuntimeException("Exception CoOmmand");
//        getContext().getLog().info("worker get one msg");
//        return Behaviors.same();
    }
}
