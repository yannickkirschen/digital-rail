package sh.yannick.rail.interlocking.track;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import sh.yannick.rail.interlocking.configuration.Document;
import sh.yannick.rail.interlocking.messaging.Message;
import sh.yannick.rail.interlocking.messaging.Messenger;
import sh.yannick.rail.interlocking.signalling.SignallingSystem;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TrackService {
    private final JmsTemplate jms;
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

    public AllocationResponse allocate(String from, String to) {
        jms.convertAndSend("allocation-request", new AllocationRequest(UUID.randomUUID(), from, to));
        return (AllocationResponse) jms.receiveAndConvert("allocation-response");
    }

    public void release(String element) throws AllocationException {
        jms.convertAndSend("release-request", new ReleaseRequest(UUID.randomUUID(), element));
        Object response = jms.receiveAndConvert("release-response");

        if (response instanceof AllocationException) {
            throw (AllocationException) response;
        }
    }
}
