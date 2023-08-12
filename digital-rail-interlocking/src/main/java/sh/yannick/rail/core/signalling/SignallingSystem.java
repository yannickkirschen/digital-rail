package sh.yannick.rail.core.signalling;

import sh.yannick.rail.core.track.TrackVertex;

import java.util.List;
import java.util.Map;

public interface SignallingSystem {
    Map<String, Integer> translate(List<TrackVertex> path);

    int getStop();
}
