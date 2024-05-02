package org.example.cluster.sharding;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.state.javadsl.CommandHandler;
import akka.persistence.typed.state.javadsl.DurableStateBehavior;
import akka.persistence.typed.state.javadsl.Effect;
import akka.serialization.jackson.CborSerializable;

public class DurableUser extends DurableStateBehavior<DurableUser.Command, DurableUser.State> {

    public interface Command extends CborSerializable {}

    public static class CountCommand implements Command {}

    public static class State implements CborSerializable {
        int count = 0;

        public State(int count) {
            this.count = count;
        }

        public int getCount() {
            return count;
        }
    }

    public static final EntityTypeKey<Command> DURABLE_USER_ENTITY_TYPE_KEY = EntityTypeKey.create(Command.class, "DurableUser");

    public static Behavior<Command> create(PersistenceId persistenceId) {
        return Behaviors.setup(context -> new DurableUser(context, persistenceId));
    }

    private ActorContext<Command> context;

    public DurableUser(ActorContext<Command> context, PersistenceId persistenceId) {
        super(persistenceId);
        this.context = context;
    }

    @Override
    public State emptyState() {
        return new State(0);
    }

    @Override
    public CommandHandler<Command, State> commandHandler() {
        return newCommandHandlerBuilder()
                .forAnyState()
                .onCommand(Command.class, (state, command) -> {
                    context.getLog().info("Durable User Count = " + state.getCount());
                    return Effect().persist(new State(state.getCount() + 1));
                })
                .build();
    }

}
