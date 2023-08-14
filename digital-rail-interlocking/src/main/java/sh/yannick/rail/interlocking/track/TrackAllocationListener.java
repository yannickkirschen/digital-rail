package sh.yannick.rail.interlocking.track;

import lombok.RequiredArgsConstructor;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TrackAllocationListener {
    private final JmsTemplate jms;
    private final TrackAllocation allocation;

    @JmsListener(destination = "allocation-request", containerFactory = "jmsFactory")
    public void handleAllocationRequest(AllocationRequest request) {
        AllocationResponse response;

        try {
            List<String> path = allocation.allocate(request.from(), request.to());
            response = new AllocationResponse(request.uuid(), path, null);
        } catch (AllocationException e) {
            response = new AllocationResponse(request.uuid(), null, e);
        }

        jms.convertAndSend("allocation-response", response);
    }

    @JmsListener(destination = "release-request", containerFactory = "jmsFactory")
    public void handleReleaseRequest(ReleaseRequest request) {
        try {
            allocation.release(request.element());
            jms.convertAndSend("release-response", request.uuid());
        } catch (AllocationException e) {
            jms.convertAndSend("release-response", e);
        }
    }
}
