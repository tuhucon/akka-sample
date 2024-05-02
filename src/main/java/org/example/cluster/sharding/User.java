package org.example.cluster.sharding;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.receptionist.Receptionist;
import akka.actor.typed.receptionist.ServiceKey;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import akka.serialization.jackson.CborSerializable;

public class User extends AbstractBehavior<User.Command> {

    public interface Command extends CborSerializable {}

    public static class CountCommand implements Command {}

    public static final EntityTypeKey<Command> USER_ENTITY_TYPE_KEY = EntityTypeKey.create(Command.class, "user");

    public static final ServiceKey<Command> USER_SERVICE_KEY = ServiceKey.create(Command.class, "user_service_key");

    public static Behavior<Command> create(String userId) {
        return Behaviors.setup(ctx -> new User(ctx, userId));
    }

    public static Behavior<Command> createInReceptionist(String userId) {
        return Behaviors.setup(ctx -> {
            ctx.getSystem().receptionist().tell(Receptionist.register(USER_SERVICE_KEY, ctx.getSelf()));
            return new User(ctx, userId);
        });
    }
    String id;

    int count = 0;

    private User(ActorContext<Command> context, String id) {
        super(context);
        this.id = id;
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(CountCommand.class, this::onCountCommand)
                .build();
    }

    public Behavior<Command> onCountCommand(CountCommand cmd) {
        count++;
        getContext().getLog().info("User " + id + " has count = " + count);
        return Behaviors.same();
    }
}
