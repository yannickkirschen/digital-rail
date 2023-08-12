package sh.yannick.rail.core.messaging;

import sh.yannick.rail.core.configuration.InventoryElement;

import java.util.List;

public record Message(String kind, Object payload) {
    public static Message error(String error) {
        return new Message("ErrorMessage", new ErrorMessage(error));
    }

    public static Message command(String command) {
        return new Message("CommandMessage", new CommandMessage(command));
    }

    public static Message setup(List<InventoryElement> setup) {
        return new Message("SetupMessage", new SetupMessage(setup));
    }

    public static Message allocation(String receiver, int state) {
        return new Message("AllocationMessage", new AllocationMessage(receiver, state));
    }
}
