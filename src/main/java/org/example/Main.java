package org.example;

import akka.Done;
import akka.NotUsed;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.ClusterEvent;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.Entity;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import akka.cluster.sharding.typed.javadsl.ShardedDaemonProcess;
import akka.cluster.typed.Cluster;
import akka.cluster.typed.ClusterSingleton;
import akka.cluster.typed.ClusterSingletonSettings;
import akka.cluster.typed.SingletonActor;
import akka.cluster.typed.Subscribe;
import akka.management.javadsl.AkkaManagement;
import akka.persistence.jdbc.state.DurableStateQueries;
import akka.persistence.jdbc.state.javadsl.JdbcDurableStateStore;
import akka.persistence.jdbc.testkit.javadsl.SchemaUtils;
import akka.persistence.state.javadsl.DurableStateUpdateStore;
import akka.persistence.typed.PersistenceId;
import akka.stream.ActorMaterializer$;
import akka.stream.ActorMaterializerHelper;
import akka.stream.Materializer;
import akka.stream.javadsl.Broadcast;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.RunnableGraph;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import org.example.cluster.DirectorBehavior;
import org.example.cluster.WorkerBehavior;
import org.example.cluster.sharding.DurableUser;
import org.example.cluster.sharding.EventSourceUser;
import org.example.cluster.sharding.User;
import scala.Int;

import java.io.IOException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ThreadLocalRandom;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        ActorSystem<Void> actorSystem = ActorSystem.create(Behaviors.empty(), "test");

        Source<Integer, NotUsed> source = Source.range(1, 100);

        Sink<Integer, CompletionStage<Integer>> sink = Sink.fold(0, Integer::sum);

        RunnableGraph<CompletionStage<Integer>> runnableGraph = source.toMat(sink, Keep.right());

        CompletionStage<Integer> result = runnableGraph.run(actorSystem);




        result.thenAccept(sum -> {
            System.out.println("SUM = " + sum);
            System.out.println(actorSystem.printTree());
        });

        try {
            System.in.read();
        } catch (IOException e) {

        } finally {
            System.out.println(actorSystem.printTree());
            actorSystem.terminate();
        }
    }
}