package sh.yannick.rail.core.signalling;

import org.springframework.stereotype.Component;
import sh.yannick.rail.core.track.StopVertex;
import sh.yannick.rail.core.track.TrackVertex;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("SimpleSignallingSystem")
public class SimpleSignallingSystem implements SignallingSystem {
    @Override
    public Map<String, Integer> translate(List<TrackVertex> path) {
        Map<String, Integer> desiredState = new HashMap<>();

        for (int i = 0; i < path.size(); i++) {
            TrackVertex current = path.get(i);
            TrackVertex previous = i > 0 ? path.get(i - 1) : null;


            if (current instanceof StopVertex stopVertex) {
                int state = (previous != null && stopVertex.getPointsTo().equals(previous.getLabel())) || path.size() - 1 == i ? 0 : 1;
                desiredState.put(stopVertex.getLabel(), state);
            }
        }

        return desiredState;
    }

    @Override
    public int getStop() {
        return 0;
    }
}
