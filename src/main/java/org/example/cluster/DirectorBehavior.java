package org.example.cluster;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.javadsl.Routers;
import akka.serialization.jackson.CborSerializable;
import akka.serialization.jackson.JsonSerializable;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class DirectorBehavior extends AbstractBehavior<DirectorBehavior.Command> {

    private final LinkedList<String> lagQueue;

    private final Map<String, Integer> result;

    private final ActorRef<WorkerBehavior.Command> workerRouter;

    public DirectorBehavior(ActorContext<Command> context) {
        super(context);
        this.lagQueue = new LinkedList<>();
        this.result = new HashMap<>();
        this.workerRouter = context.spawn(Routers.group(WorkerBehavior.SERVICE_KEY).withRoundRobinRouting(), "worker-router");
    }

    public static Behavior<Command> create() {
        return Behaviors.withTimers(timer -> Behaviors.setup(ctx -> {
            timer.startTimerAtFixedRate(new Tick(), Duration.ofSeconds(0L), Duration.ofSeconds(ctx.getSystem().settings().config().getLong("service.director.tick")));
            return new DirectorBehavior(ctx);
        }));
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(Tick.class, this::onTick)
                .onMessage(TextProcessResponse.class, this::onTextProcessResponse)
                .onMessage(TextProcessFailure.class, this::onTextProcessFailure)
                .build();
    }

    private Behavior<Command> onTextProcessFailure(TextProcessFailure cmd) {
        lagQueue.add(cmd.getText());
        getContext().getLog().warn("Lag Queue: " + lagQueue.size());
        return Behaviors.same();
    }

    private Behavior<Command> onTextProcessResponse(TextProcessResponse cmd) {
        for (Map.Entry<String, Integer> entry : cmd.getTextResponse().getTextCount().entrySet()) {
            if (result.containsKey(entry.getKey())) {
                result.put(entry.getKey(), result.get(entry.getKey()) + entry.getValue());
            } else {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        getContext().getLog().info(result.toString());
        return Behaviors.same();
    }

    private Behavior<Command> onTick(Tick cmd) {
        if(ThreadLocalRandom.current().nextInt(10) < 5) {
            throw new RuntimeException("exception from tick in director");
        }
        getContext().getLog().info("director get Tick, will send msg to worker");
        final String text = !lagQueue.isEmpty() ? lagQueue.poll() : "hom nay troi khong mua";
        getContext().ask(
                WorkerBehavior.TextProcessResponse.class,
                workerRouter,
                Duration.of(getContext().getSystem().settings().config().getLong("service.director.wait-timeout"), ChronoUnit.SECONDS),
                me -> new WorkerBehavior.TextProcessCommand(text, me),
                (response, err) -> {
                    if (err == null) {
                        return new TextProcessResponse(response);
                    } else {
                        return new TextProcessFailure(text);
                    }
                }
        );
        return Behaviors.same();
    }

    public interface Command extends CborSerializable {

    }

    private static class TextProcessFailure implements Command {

        String text;

        public TextProcessFailure(String text) {
            this.text = text;
        }

        public String getText() {
            return this.text;
        }
    }

    private static class TextProcessResponse implements Command {

        WorkerBehavior.TextProcessResponse textResponse;

        public TextProcessResponse(WorkerBehavior.TextProcessResponse textResponse) {
            this.textResponse = textResponse;
        }

        public WorkerBehavior.TextProcessResponse getTextResponse() {
            return this.textResponse;
        }
    }

    public static class Tick implements Command {

    }
}
