package sh.yannick.rail.interlocking.messaging;

public record AllocationMessage(String receiver, int state) {
}
