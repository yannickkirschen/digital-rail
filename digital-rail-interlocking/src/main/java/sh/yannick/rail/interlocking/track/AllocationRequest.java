package sh.yannick.rail.interlocking.track;

import java.util.UUID;

public record AllocationRequest(UUID uuid, String from, String to) {
}
