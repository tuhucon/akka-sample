package org.example.receptionist;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.receptionist.Receptionist;
import akka.actor.typed.receptionist.ServiceKey;

public class MainBehavior extends AbstractBehavior<MainBehavior.Command> {

    public interface Command {}

    public static class PushYCommandToAllWorker implements Command {}

    private static class ReceptionistListingAdapter implements Command {

        Receptionist.Listing listing;

        public ReceptionistListingAdapter(Receptionist.Listing listing) {
            this.listing = listing;
        }

        Receptionist.Listing getListing() {
            return listing;
        }
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(context -> {
            context.spawn(WorkerGeneratorBehavior.create(), "worker-generator");
            ActorRef<Receptionist.Listing> adapter = context.messageAdapter(Receptionist.Listing.class, ReceptionistListingAdapter::new);
            context.getSystem().receptionist().tell(Receptionist.subscribe(WorkerBehavior.workerServiceKey, adapter));
            return new MainBehavior(context);
        });
    }

    public MainBehavior(ActorContext<Command> context) {
        super(context);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(PushYCommandToAllWorker.class, this::onPushYCommandToAllWorker)
                .onMessage(ReceptionistListingAdapter.class, this::onReceptionistListing)
                .build();
    }

    private Behavior<Command> onPushYCommandToAllWorker(PushYCommandToAllWorker cmd) {
        //need to use Adapter to convert Receptionist.Listing response to a private command
        ActorRef<Receptionist.Listing> adapter = getContext().messageAdapter(Receptionist.Listing.class, ReceptionistListingAdapter::new);
        getContext().getSystem().receptionist().tell(Receptionist.find(WorkerBehavior.workerServiceKey, adapter));
        return Behaviors.same();
    }

    private Behavior<Command> onReceptionistListing(ReceptionistListingAdapter cmd) {
        getContext().getLog().info("Receptionist Response: " + cmd.getListing().getClass());
        cmd.getListing().getServiceInstances(WorkerBehavior.workerServiceKey).forEach(worker -> {
            worker.tell(new WorkerBehavior.YCommand());
        });
        return Behaviors.same();
    }
}
