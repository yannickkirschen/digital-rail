package sh.yannick.rail.interlocking.track;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sh.yannick.rail.interlocking.configuration.Document;
import sh.yannick.rail.interlocking.messaging.Message;
import sh.yannick.rail.interlocking.messaging.Messenger;
import sh.yannick.rail.interlocking.signalling.SignallingSystem;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TrackService {
    private final TrackAllocation allocation;
    private final SignallingSystem signallingSystem;
    private final Messenger messenger;
    private final Document document;

    @PostConstruct
    public void init() throws IOException {
        messenger.send(Message.setup(document.getInventory()));
    }

    @PreDestroy
    public void destroy() throws IOException {
        messenger.send(Message.command("reset"));
        messenger.disconnect();
    }

    public List<String> allocate(String from, String to) throws AllocationException {
        List<TrackVertex> path = allocation.allocate(from, to);

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
        allocation.release(element);
        Message message = Message.allocation(element, signallingSystem.getStop());
        try {
            Message reply = messenger.send(message);
            // TODO handle error messages
            System.out.println(reply);
        } catch (IOException e) {
            throw new AllocationException(e);
        }
    }
}
