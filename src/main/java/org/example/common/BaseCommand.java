package org.example.common;

import java.util.UUID;

public abstract class BaseCommand {

    UUID commandId;

    public BaseCommand(UUID commandId) {
        this.commandId = commandId;
    }

    public UUID getCommandId() {
        return commandId;
    }
}
