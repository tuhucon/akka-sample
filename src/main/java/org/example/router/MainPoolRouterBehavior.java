package org.example.router;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.PoolRouter;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.javadsl.Routers;

public class MainPoolRouterBehavior {

    public static ActorRef<WorkerBehavior.Command> router;
    public static Behavior<Void> create() {
        return Behaviors.setup(context -> {
            var poolRouter = Routers.pool(3, WorkerBehavior.create()).withRoundRobinRouting();
            router = context.spawn(poolRouter, "pool-router");
            return Behaviors.empty();
        });
    }

}
