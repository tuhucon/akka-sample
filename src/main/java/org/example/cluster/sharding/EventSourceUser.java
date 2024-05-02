package org.example.cluster.sharding;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import akka.cluster.singleton.ClusterSingletonManager;
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.javadsl.CommandHandler;
import akka.persistence.typed.javadsl.Effect;
import akka.persistence.typed.javadsl.EventHandler;
import akka.persistence.typed.javadsl.EventSourcedBehavior;
import akka.persistence.typed.javadsl.RetentionCriteria;
import akka.serialization.jackson.CborSerializable;

public class EventSourceUser extends EventSourcedBehavior<EventSourceUser.Command, EventSourceUser.Event, EventSourceUser.State> {

    public interface Command extends CborSerializable {}
    public interface Event extends CborSerializable {}

    public static class CountedEvent implements Event {}

    public static class CountCommand implements Command {}

    public static class State implements CborSerializable {
        int count;

        public State() {
            this.count = 0;
        }

        public State(int count) {
            this.count = count;
        }

        public int getCount() {
            return count;
        }
    }

    public static final EntityTypeKey<Command> EVENT_SOURCE_USER_ENTITY_TYPE_KEY = EntityTypeKey.create(Command.class, "EventSourceUser");

    public static Behavior<Command> create(PersistenceId persistenceId) {
        return Behaviors.setup(context -> new EventSourceUser(context, persistenceId));
    }

    private ActorContext<Command> context;

    public EventSourceUser(ActorContext<Command> context, PersistenceId persistenceId) {
        super(persistenceId);
        this.context = context;
    }

    public ActorContext<Command> getContext() {
        return context;
    }

    @Override
    public State emptyState() {
        return new State(0);
    }

    @Override
    public CommandHandler<Command, Event, State> commandHandler() {
        return newCommandHandlerBuilder()
                .forAnyState()
                .onCommand(CountCommand.class, this::onCountCommand)
                .build();
    }

    @Override // override retentionCriteria in EventSourcedBehavior
    public RetentionCriteria retentionCriteria() {
        return RetentionCriteria.snapshotEvery(10, 2);
    }

    @Override // override shouldSnapshot in EventSourcedBehavior
    public boolean shouldSnapshot(State state, Event event, long sequenceNr) {
        return true;
    }

    @Override
    public EventHandler<State, Event> eventHandler() {
        return newEventHandlerBuilder()
                .forAnyState()
                .onEvent(CountedEvent.class,this::onCountedEvent)
                .build();
    }

    private Effect<Event, State> onCountCommand(State currentState, CountCommand cmd) {
        return Effect()
                .persist(new CountedEvent())
                .thenRun(newState -> {
                    getContext().getLog().info("current state: " + currentState.getCount());
                    getContext().getLog().info("new state: " + newState.getCount());
                });
    }

    private State onCountedEvent(State currentState, CountedEvent event) {
        return new State(currentState.getCount() + 1);
    }

}
