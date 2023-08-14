package sh.yannick.rail.interlocking.track;

import java.util.List;
import java.util.UUID;

public record AllocationResponse(UUID uuid, List<String> path, Throwable error) {
}
