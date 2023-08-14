package sh.yannick.rail.api;

import java.util.List;

public record AllocationApiResponse(List<String> path, String error) {
}
