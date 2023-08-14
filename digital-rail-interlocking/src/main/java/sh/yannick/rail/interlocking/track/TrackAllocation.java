package sh.yannick.rail.interlocking.track;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sh.yannick.rail.interlocking.messaging.Message;
import sh.yannick.rail.interlocking.messaging.Messenger;
import sh.yannick.rail.interlocking.signalling.SignallingSystem;
import sh.yannick.tools.math.Graph;
import sh.yannick.tools.math.GraphWalkingDecision;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class TrackAllocation {
    private final GraphWalkingDecision<TrackVertex> switchDecision = (from, to, via) -> !via.getProhibits().containsKey(from.getLabel()) || !via.getProhibits().get(from.getLabel()).equals(to.getLabel());

    @Getter
    private final Graph<TrackVertex> graph;
    private final SignallingSystem signallingSystem;
    private final Messenger messenger;

    public List<String> allocate(String from, String to) throws AllocationException {
        TrackVertex fromTrack = graph.findVertex(from);
        TrackVertex toTrack = graph.findVertex(to);

        List<List<TrackVertex>> paths = graph.findPaths(fromTrack.getLabel(), toTrack.getLabel(), switchDecision);

        if (paths.isEmpty()) {
            throw new AllocationException("No path found from %s to %s", from, to);
        }

        // TODO: Handle multiple paths
        List<TrackVertex> path = paths.stream().findFirst().get();

        if (path.stream().anyMatch(TrackVertex::isLocked)) {
            throw new AllocationException("Path %s -> %s is already allocated", from, to);
        }


        Map<String, Integer> desiredState = new HashMap<>(signallingSystem.translate(path));
        for (int i = 0; i < path.size(); i++) {
            TrackVertex current = path.get(i);
            TrackVertex previous = i > 0 ? path.get(i - 1) : null;
            TrackVertex next = i < path.size() - 1 ? path.get(i + 1) : null;

            if (current instanceof SwitchVertex switchVertex) {
                int state = (next == null || !switchVertex.getBaseVertex().equals(next.getLabel())) && (previous == null || !switchVertex.getBaseVertex().equals(previous.getLabel())) ? 1 : 0;
                desiredState.put(switchVertex.getLabel(), state);
            }
        }

        path.forEach(v -> v.setLocked(true));

        Set<Message> messages = desiredState
            .keySet()
            .stream()
            .map(label -> Message.allocation(label, desiredState.get(label)))
            .collect(java.util.stream.Collectors.toSet());

        try {
            Set<Message> replies = messenger.send(messages);
            // TODO handle error messages
            System.out.println(replies);
        } catch (IOException e) {
            path.forEach(v -> v.setLocked(false));
            throw new AllocationException(e);
        }

        return path.stream().map(TrackVertex::getLabel).toList();
    }

    public void release(String element) throws AllocationException {
        TrackVertex vertex = graph.findVertex(element);
        if (vertex.isLocked()) {
            vertex.setLocked(false);
        } else {
            throw new AllocationException("Element %s is not locked", element);
        }

        Message message = Message.allocation(element, signallingSystem.getStop()); // TODO: Handle switches
        try {
            Message reply = messenger.send(message);
            // TODO handle error messages
            System.out.println(reply);
        } catch (IOException e) {
            throw new AllocationException(e);
        }
    }
}
