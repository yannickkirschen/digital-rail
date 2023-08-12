package sh.yannick.rail.core.messaging;

import sh.yannick.rail.core.configuration.InventoryElement;

import java.util.List;

public record SetupMessage(List<InventoryElement> elements) {
}
