package org.example.textparser;

import akka.actor.testkit.typed.javadsl.ActorTestKit;
import akka.actor.testkit.typed.javadsl.LoggingTestKit;
import akka.actor.typed.ActorRef;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;

import java.util.List;
import java.util.UUID;

public class ManagerBehaviorTest {

    static final ActorTestKit testKit = ActorTestKit.create();

    @Test
    public void test1() throws InterruptedException {
        LoggingTestKit.empty()
                .withLogLevel(Level.INFO)
                .withMessageContains("completed parse command")
                .expect(testKit.system(), () -> {
                    ActorRef<ManagerBehavior.Command> manager = testKit.spawn(ManagerBehavior.create());
                    manager.tell(new ManagerBehavior.ParseCommand(UUID.randomUUID(), List.of("tu hu con", "chich choe")));
                    return null;
                });
    }

    @AfterAll
    public static void clean() {
        testKit.shutdownTestKit();
    }

}
