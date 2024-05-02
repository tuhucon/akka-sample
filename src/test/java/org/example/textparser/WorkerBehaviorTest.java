package org.example.textparser;

import akka.actor.testkit.typed.TestKitSettings;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import akka.actor.testkit.typed.javadsl.ActorTestKit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.UUID;

public class WorkerBehaviorTest {

    static final ActorTestKit testKit = ActorTestKit.create();

    @Test
    public void test1() {
        ActorRef<WorkerBehavior.Command> worker = testKit.spawn(WorkerBehavior.create());
        TestProbe<WorkerBehavior.ParseCommandResponse> probe = testKit.createTestProbe();
        UUID uuid = UUID.randomUUID();
        worker.tell(new WorkerBehavior.ParseCommand(uuid, probe.getRef(), "Tu Hu Con"));
        WorkerBehavior.ParseCommandResponse response = probe.receiveMessage(Duration.ofSeconds(10_000L));
        Assertions.assertEquals(response.getParsedContent(), "TU HU CON");
        Assertions.assertEquals(response.getCommandId(), uuid);
    }

    @AfterAll
    public static void clean() {
        testKit.shutdownTestKit();
    }
}
