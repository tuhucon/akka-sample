package org.example;

import akka.NotUsed;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import akka.stream.ClosedShape;
import akka.stream.FlowShape;
import akka.stream.UniformFanInShape;
import akka.stream.UniformFanOutShape;
import akka.stream.javadsl.Broadcast;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.GraphDSL;
import akka.stream.javadsl.Merge;
import akka.stream.javadsl.RunnableGraph;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

import java.io.IOException;
import java.util.concurrent.CompletionStage;

public class Main {

    public static RunnableGraph<CompletionStage<Integer>> createOneSource2FlowGraph() {
        Source<Integer, NotUsed> source = Source.range(1, 100);
        Flow<Integer, Integer, NotUsed> flow1 = Flow.of(Integer.class).map(i -> i * 2);
        Flow<Integer, Integer, NotUsed> flow2 = Flow.of(Integer.class).map(i -> i * 3);
        Sink<Integer, CompletionStage<Integer>> sink = Sink.fold(0, Integer::sum);

        //use GrapDSL.create(sink,(builder,out)... when we want to get output from Graph.
        //use GrapDSL,create(builder... when we dont want to get output from Graph.
        RunnableGraph<CompletionStage<Integer>> graph = RunnableGraph.fromGraph(GraphDSL.create(sink, (builder, out) -> {
            //Create Junctions
            UniformFanOutShape<Integer, Integer> broadCast = builder.add(Broadcast.create(2));
            UniformFanInShape<Integer, Integer> merge = builder.add(Merge.create(2));

            builder.from(builder.add(source).out())
                    .viaFanOut(broadCast)
                    .via(builder.add(flow1))
                    .viaFanIn(merge)
                    .to(out);

            builder.from(broadCast)
                    .via(builder.add(flow2))
                    .viaFanIn(merge);


            return ClosedShape.getInstance();
        }));
        return graph;
    }

    public static void main(String[] args) throws InterruptedException {
        ActorSystem<Void> actorSystem = ActorSystem.create(Behaviors.empty(), "test");

        CompletionStage<Integer> result = createOneSource2FlowGraph().run(actorSystem);

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