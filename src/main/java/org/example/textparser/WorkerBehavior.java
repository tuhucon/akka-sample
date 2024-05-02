package org.example.textparser;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import org.example.common.BaseCommand;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class WorkerBehavior extends AbstractBehavior<WorkerBehavior.Command> {

    public static abstract class Command extends BaseCommand {

        public Command(UUID commandId) {
            super(commandId);
        }
    }

    public static abstract class Response extends BaseCommand {

        public Response(UUID commandId) {
            super(commandId);
        }
    }

    public static class ParseCommand extends Command {

        String content;

        ActorRef<ParseCommandResponse> replyTo;

        public ParseCommand(UUID commandId, ActorRef<ParseCommandResponse> replyTo, String content) {
            super(commandId);
            this.replyTo = replyTo;
            this.content = content;
        }
    }

    public static class ParseCommandResponse extends Response {

        String parsedContent;

        public ParseCommandResponse(UUID commandId, String parsedContent) {
            super(commandId);
            this.parsedContent = parsedContent;
        }

        public String getParsedContent() {
            return parsedContent;
        }
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(WorkerBehavior::new);
    }

    public WorkerBehavior(ActorContext<Command> context) {
        super(context);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(ParseCommand.class, this::onParseCommand)
                .onSignal(PostStop.class, this::onPostStop)
                .build();
    }

    private Behavior<Command> onParseCommand(ParseCommand cmd) {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextLong(4_000L, 8_000L));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        String parsedContent = cmd.content.toUpperCase();
        getContext().getLog().info("parse done for line: " + cmd.content);
        cmd.replyTo.tell(new ParseCommandResponse(cmd.getCommandId(), parsedContent));
        return Behaviors.stopped();
    }

    private Behavior<Command> onPostStop(PostStop signal) {
        getContext().getLog().info("Worker: " + this.toString() + " stop with signal: " + signal.toString());
        return this;
    }
}
