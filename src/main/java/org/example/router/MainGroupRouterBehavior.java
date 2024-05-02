package org.example.router;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.GroupRouter;
import akka.actor.typed.javadsl.Routers;
import akka.actor.typed.receptionist.Receptionist;
import akka.actor.typed.receptionist.ServiceKey;

public class MainGroupRouterBehavior {

    public static ActorRef<WorkerBehavior.Command> router;

    public static Behavior<Void> create() {
        return Behaviors.setup(context -> {

            ServiceKey<WorkerBehavior.Command> serviceKey = ServiceKey.create(WorkerBehavior.Command.class, "worker");

            for (int i = 0; i < 3; i++) {
                ActorRef<WorkerBehavior.Command> worker = context.spawn(WorkerBehavior.create(), "worker-" + i);
                context.getSystem().receptionist().tell(Receptionist.register(serviceKey, worker));
            }

//            GroupRouter<WorkerBehavior.Command> groupRouter = Routers.group(serviceKey).withRoundRobinRouting(true);

            GroupRouter<WorkerBehavior.Command> groupRouter = Routers.group(serviceKey).withConsistentHashingRouting(10, cmd -> cmd.hashCode() + "");

            router = context.spawnAnonymous(groupRouter);

            return Behaviors.empty();
        });
    }

}
