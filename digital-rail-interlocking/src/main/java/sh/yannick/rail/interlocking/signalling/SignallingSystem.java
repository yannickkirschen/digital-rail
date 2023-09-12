package sh.yannick.rail.interlocking.signalling;

import sh.yannick.rail.api.resource.Block;
import sh.yannick.rail.api.resource.Signal;
import sh.yannick.state.State;

import java.util.List;

public interface SignallingSystem {
    static SignallingSystem get(State state, String name) {
        if (name.equalsIgnoreCase("simple")) {
            return new SimpleSignallingSystem(state);
        }

        throw new IllegalArgumentException("Unknown signalling system: " + name);
    }

    List<Signal> translate(List<Block> path);
}
