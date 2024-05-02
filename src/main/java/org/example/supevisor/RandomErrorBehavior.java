package org.example.supevisor;

import akka.actor.typed.Behavior;
import akka.actor.typed.Signal;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class RandomErrorBehavior extends AbstractBehavior<RandomErrorBehavior.Command> {

    public interface Command {

    }

    public static class FirstCommand implements Command {

        public static FirstCommand createRestartErr() {
            return new FirstCommand(1);
        }

        public static FirstCommand createResumeErr() {
            return new FirstCommand(2);
        }

        private final int type;

        public FirstCommand(int type) {
            this.type = type;
        }

        public int getType() {
            return type;
        }
    }

    public static Behavior<Command> createWithSupervisor() {
        return Behaviors.supervise(
                Behaviors.supervise(Behaviors.setup(RandomErrorBehavior::new))
                        .onFailure(ResumeException.class, SupervisorStrategy.resume())
        ).onFailure(RestartException.class, SupervisorStrategy.restart());
    }

    public RandomErrorBehavior(ActorContext<Command> context) {
        super(context);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(FirstCommand.class, this::onFirstCommand)
                .onSignal(Signal.class, signal -> {
                    getContext().getLog().info(signal.getClass().getSimpleName() + " from " + this.toString());
                    return this;
                })
                .build();
    }

    private Behavior<Command> onFirstCommand(FirstCommand cmd) {
        switch (cmd.type) {
            case 1:
                throw new RestartException("test restart");
            case 2:
                throw new ResumeException("test resume");
            default:
                getContext().getLog().info("run ok with type = " + cmd.type);
        }
        return this;
    }
}
