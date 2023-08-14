package sh.yannick.rail.interlocking.messaging;

import sh.yannick.rail.interlocking.configuration.InventoryElement;

import java.util.List;

public record SetupMessage(List<InventoryElement> elements) {
}
