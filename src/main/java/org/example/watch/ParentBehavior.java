package org.example.watch;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class ParentBehavior extends AbstractBehavior<ParentBehavior.Command> {

    public interface Command {

    }
    
    public static class ForwardMsgToChild implements Command {
        
    }
    
    public static class StopChild implements Command {
        
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(ParentBehavior::new);
    }

    private ActorRef<ChildBehavior.Command> child;

    public ParentBehavior(ActorContext<Command> context) {
        super(context);
        child = context.spawn(ChildBehavior.create(), "child");
        context.watch(child);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(ForwardMsgToChild.class, this::onParseMsgToChildCommand)
                .onMessage(StopChild.class, this::onStopChild)
                .build();
    }

    private Behavior<Command> onParseMsgToChildCommand(Command cmd) {
        getContext().getLog().info("parent get forward msg to child");
        child.tell(new ChildBehavior.Command() {});
        return this;
    }

    private Behavior<Command> onStopChild(Command cmd) {
        getContext().getLog().info("parent will stop child");
        getContext().stop(child);
        return this;
    }
}
