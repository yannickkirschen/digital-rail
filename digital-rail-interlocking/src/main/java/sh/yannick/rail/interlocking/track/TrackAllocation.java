package sh.yannick.rail.interlocking.track;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sh.yannick.tools.math.Graph;
import sh.yannick.tools.math.GraphWalkingDecision;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TrackAllocation {
    private final GraphWalkingDecision<TrackVertex> switchDecision = (from, to, via) -> !via.getProhibits().containsKey(from.getLabel()) || !via.getProhibits().get(from.getLabel()).equals(to.getLabel());

    @Getter
    private final Graph<TrackVertex> graph;

    public List<TrackVertex> allocate(String from, String to) throws AllocationException {
        TrackVertex fromTrack = graph.findVertex(from);
        TrackVertex toTrack = graph.findVertex(to);

        List<List<TrackVertex>> paths = graph.findPaths(fromTrack.getLabel(), toTrack.getLabel(), switchDecision);

        if (paths.isEmpty()) {
            throw new AllocationException("No path found from %s to %s", from, to);
        }

        // TODO: Handle multiple paths
        return paths.stream().findFirst().get();
    }

    public void release(String element) throws AllocationException {
        TrackVertex vertex = graph.findVertex(element);
        if (vertex.isLocked()) {
            vertex.setLocked(false);
        } else {
            throw new AllocationException("Element %s is not locked", element);
        }
    }
}
