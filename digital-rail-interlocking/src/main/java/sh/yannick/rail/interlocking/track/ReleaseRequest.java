package sh.yannick.rail.interlocking.track;

import java.util.UUID;

public record ReleaseRequest(UUID uuid, String element) {
}
