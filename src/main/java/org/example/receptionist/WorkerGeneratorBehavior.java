package org.example.receptionist;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.javadsl.TimerScheduler;
import akka.actor.typed.receptionist.Receptionist;
import akka.actor.typed.receptionist.ServiceKey;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

public class WorkerGeneratorBehavior extends AbstractBehavior<WorkerGeneratorBehavior.Command> {

    public interface Command {}

    private static class GenerateWorkerCommand implements Command {}

    public static Behavior<Command> create() {
        return Behaviors.withTimers(timer -> {
            timer.startSingleTimer(new GenerateWorkerCommand(), Duration.ofSeconds(ThreadLocalRandom.current().nextLong(1L, 3L)));
            return Behaviors.setup(context -> new WorkerGeneratorBehavior(context, timer));
        });
    }

    private int currentWorker = 1;

    private int maxWorker = 10;

    private TimerScheduler<Command> timer;

    public WorkerGeneratorBehavior(ActorContext<Command> context, TimerScheduler<Command> timer) {
        super(context);
        this.timer = timer;
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(GenerateWorkerCommand.class, this::onGenerateWorkerCommand)
                .build();
    }

    private Behavior<Command> onGenerateWorkerCommand(GenerateWorkerCommand cmd) {
        getContext().getLog().info("on generating a worker from " + this);
        ActorRef<WorkerBehavior.Command> worker = getContext().spawn(WorkerBehavior.create(), "worker_" + currentWorker);
        getContext().getSystem().receptionist().tell(Receptionist.register(WorkerBehavior.workerServiceKey, worker));
        Receptionist.register(ServiceKey.create(WorkerBehavior.Command.class, "worker"), worker);
        currentWorker += 1;
        if (currentWorker <= maxWorker) {
            timer.startSingleTimer(new GenerateWorkerCommand(), Duration.ofSeconds(ThreadLocalRandom.current().nextLong(1L, 3L)));
        }
        return Behaviors.same();
    }
}
