package org.example.textparser;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import org.example.common.BaseCommand;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ManagerBehavior extends AbstractBehavior<ManagerBehavior.Command> {

    public static abstract class Command extends BaseCommand {

        public Command(UUID commandId) {
            super(commandId);
        }
    }

    public static class ParseCommand extends Command {

        List<String> content;

        public ParseCommand(UUID commandId, List<String> content) {
            super(commandId);
            this.content = content;
        }
    }

    private static class ParsedContent extends Command {

        String parsedContent;

        public ParsedContent(UUID commandId, String parsedContent) {
            super(commandId);
            this.parsedContent = parsedContent;
        }
    }

    private static class ParsedContentError extends ParsedContent {
        Throwable e;

        String rawLine;

        public ParsedContentError(UUID commandId, String rawLine, Throwable e) {
            super(commandId, null);
            this.e = e;
            this.rawLine = rawLine;
        }

        public Throwable getError() {
            return e;
        }
    }


    private static class BufferContent {
        int count;
        List<String> contents;

        public BufferContent(int count) {
            this.count = count;
            this.contents = new ArrayList<>();
        }
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(ManagerBehavior::new);
    }

    private final Map<UUID, BufferContent> bufferContents;

    public ManagerBehavior(ActorContext<Command> context) {
        super(context);
        bufferContents = new HashMap<>();
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(ParseCommand.class, this::onParseCommand)
                .onMessage(ParsedContent.class, this::onParsedContent)
                .build();
    }

    private Behavior<Command> onParseCommand(ParseCommand cmd) {
        if (!cmd.content.isEmpty()) {
            bufferContents.put(cmd.getCommandId(), new BufferContent(cmd.content.size()));
            int i = 1;
            for (String line: cmd.content) {
                ActorRef<WorkerBehavior.Command> worker = getContext().spawn(WorkerBehavior.create(), "worker_" + i + "_" + cmd.getCommandId().toString());
                i++;
                getContext().ask(WorkerBehavior.ParseCommandResponse.class, worker, Duration.ofSeconds(10L),
                        me -> new WorkerBehavior.ParseCommand(cmd.getCommandId(), me, line),
                        (response, throwable) -> {
                            if (throwable == null) {
                                return new ParsedContent(response.getCommandId(), response.getParsedContent());
                            } else {
                                return new ParsedContentError(cmd.getCommandId(), line, throwable);
                            }
                        }

                );
            }
        }
        return this;
    }

    private Behavior<Command> onParsedContent(ParsedContent cmd) {
        if (cmd instanceof ParsedContentError error) {
            getContext().getLog().error(error.getError().getMessage()); //memory leak, we cannot clear buffer content if have error
        } else {
            getContext().getLog().info("get parsed line: " + cmd.parsedContent);
            BufferContent bufferContent = bufferContents.get(cmd.getCommandId());
            bufferContent.contents.add(cmd.parsedContent);
            if (bufferContent.contents.size() >= bufferContent.count) {
                getContext().getLog().info("completed parse command: " + cmd.getCommandId());
                getContext().getLog().info(bufferContent.contents.toString());
                bufferContents.remove(cmd.getCommandId());
            }
        }
        return this;
    }

}
