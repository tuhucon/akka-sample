package org.example;

import akka.Done;
import akka.NotUsed;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import akka.japi.Pair;
import akka.stream.ClosedShape;
import akka.stream.FlowShape;
import akka.stream.UniformFanInShape;
import akka.stream.UniformFanOutShape;
import akka.stream.javadsl.BidiFlow;
import akka.stream.javadsl.Broadcast;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.GraphDSL;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Broadcast;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.GraphDSL;
import akka.stream.javadsl.Merge;
import akka.stream.javadsl.RunnableGraph;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ThreadLocalRandom;

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

    public static RunnableGraph<CompletionStage<Integer>> createThreeSourceOneSinkGraph() {
        Source<Integer, NotUsed> source1 = Source.range(1, 10);
        Source<Integer, NotUsed> source2 = Source.range(11, 20);
        Source<Integer, NotUsed> source3 = Source.range(21, 30);
        Sink<Integer, CompletionStage<Integer>> sink = Sink.fold(0, Integer::max);

        return RunnableGraph.fromGraph(GraphDSL.create(sink, (builder, out) -> {
            UniformFanInShape<Integer, Integer> merge = builder.add(Merge.create(3));

            builder.from(builder.add(source1).out())
                    .viaFanIn(merge)
                    .to(out);

            builder.from(builder.add(source2).out())
                    .viaFanIn(merge);

            builder.from(builder.add(source3).out())
                    .viaFanIn(merge);

            return ClosedShape.getInstance();
        }));
    }

    public static RunnableGraph<CompletionStage<Integer>> createOneRandomSourceGraph() {
        Source<Integer, NotUsed> source = Source.unfold(0, s -> {
            Integer random = ThreadLocalRandom.current().nextInt(10);
            if (random > 8 && s > 0) {
                return Optional.empty();
            } else {
                return Optional.of(new Pair<Integer, Integer>(s + random, random));
            }
        });

        Sink<Integer, CompletionStage<Integer>> sink = Sink.fold(0, Integer::max);
        return source.toMat(sink, Keep.right());
    }

    public static RunnableGraph<NotUsed> createBidiFlowGraph() {
        Source<Integer, NotUsed> source = Source.range(1, 3);
        var bidiFlow = BidiFlow.fromFlows(Flow.of(Integer.class).map(i -> i * 3), Flow.of(Integer.class).map(i -> i * 10));
        Sink<Integer, CompletionStage<Done>> sink = Sink.foreach(i -> {
            System.out.println(i + "");
        });
        return source.via(bidiFlow.join(Flow.of(Integer.class)))
                .to(sink);
    }

    public static void main(String[] args) throws InterruptedException {
        ActorSystem<Void> actorSystem = ActorSystem.create(Behaviors.empty(), "test");

        RunnableGraph<NotUsed> randomSourceGraph = createBidiFlowGraph();
        randomSourceGraph.run(actorSystem);

        try {
            System.in.read();
        } catch (IOException e) {

        } finally {
            System.out.println(actorSystem.printTree());
            actorSystem.terminate();
        }
    }
}