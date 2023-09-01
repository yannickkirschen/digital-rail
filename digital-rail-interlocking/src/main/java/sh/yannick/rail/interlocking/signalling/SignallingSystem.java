package sh.yannick.rail.interlocking.signalling;

import sh.yannick.rail.api.resource.Block;
import sh.yannick.rail.api.resource.Signal;

import java.util.List;

public interface SignallingSystem {
    List<Signal> translate(List<Block> path);
}
