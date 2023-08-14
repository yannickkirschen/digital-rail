package sh.yannick.rail.interlocking.signalling;

import sh.yannick.rail.interlocking.track.TrackVertex;

import java.util.List;
import java.util.Map;

public interface SignallingSystem {
    Map<String, Integer> translate(List<TrackVertex> path);

    int getStop();
}
