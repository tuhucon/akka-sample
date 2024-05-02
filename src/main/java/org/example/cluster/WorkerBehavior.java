package org.example.cluster;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.receptionist.Receptionist;
import akka.actor.typed.receptionist.ServiceKey;
import akka.serialization.jackson.CborSerializable;
import akka.serialization.jackson.JsonSerializable;

import java.util.HashMap;
import java.util.Map;

public class WorkerBehavior extends AbstractBehavior<WorkerBehavior.Command> {

    interface Command extends CborSerializable {

    }

    interface Response extends CborSerializable {

    }

    public static class TextProcessCommand implements Command {
        String text;

        ActorRef<TextProcessResponse> replyTo;

        public TextProcessCommand(String text, ActorRef<TextProcessResponse> replyTo) {
            this.text = text;
            this.replyTo = replyTo;
        }
    }

    public static class TextProcessResponse implements Response {

        Map<String, Integer> textCount = new HashMap<>();

        public Integer increaseForText(String text) {
            if (textCount.containsKey(text)) {
                textCount.put(text, textCount.get(text) + 1);
            } else {
                textCount.put(text, 1);
            }
            return textCount.get(text);
        }

        public Map<String, Integer> getTextCount() {
            return textCount;
        }
    }

    public static final ServiceKey<Command> SERVICE_KEY = ServiceKey.create(Command.class, "worker");

    public static Behavior<Command> create() {
        return Behaviors.setup(WorkerBehavior::new);
    }

    public WorkerBehavior(ActorContext<Command> context) {
        super(context);
        context.getSystem().receptionist().tell(Receptionist.register(SERVICE_KEY, context.getSelf()));
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(TextProcessCommand.class, this::onTextProcessCommand)
                .build();
    }

    public Behavior<Command> onTextProcessCommand(TextProcessCommand cmd) {
        getContext().getLog().info(getContext().getSystem().name() + " is processing msg from director");
        String[] textArr = cmd.text.split(" ");
        TextProcessResponse response = new TextProcessResponse();
        for (int i = 0; i < textArr.length; i++) {
            response.increaseForText(textArr[i]);
        }
        cmd.replyTo.tell(response);
        return Behaviors.same();
    }
}
