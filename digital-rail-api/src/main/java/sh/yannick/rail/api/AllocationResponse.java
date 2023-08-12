package sh.yannick.rail.api;

import java.util.List;

public record AllocationResponse(List<String> path, String error) {
}
