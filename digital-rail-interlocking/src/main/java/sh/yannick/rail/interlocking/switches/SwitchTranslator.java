package sh.yannick.rail.interlocking.switches;

import sh.yannick.rail.api.resource.Block;
import sh.yannick.rail.api.resource.Switch;

import java.util.List;

@FunctionalInterface
public interface SwitchTranslator {
    List<Switch> translate(List<Block> path);
}
