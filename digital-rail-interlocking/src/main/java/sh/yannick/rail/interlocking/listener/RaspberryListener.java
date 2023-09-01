package sh.yannick.rail.interlocking.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.util.MimeTypeUtils;
import reactor.util.retry.Retry;
import sh.yannick.rail.api.resource.Raspberry;
import sh.yannick.state.Listener;
import sh.yannick.state.ResourceListener;
import sh.yannick.state.State;

import java.io.IOException;
import java.time.Duration;

@Slf4j
@Listener(apiVersion = "embedded.yannick.sh/v1alpha1", kind = "Raspberry")
public class RaspberryListener implements ResourceListener<Raspberry.Spec, Raspberry.Status, Raspberry> {
    private State state;

    @Override
    public void onInit(State state) {
        this.state = state;
    }

    @Override
    public void onCreate(Raspberry raspberry) {
        onUpdate(raspberry);
    }

    @Override
    public void onUpdate(Raspberry raspberry) {
        log.info("Sending {}/{}/{} to {}:{}", raspberry.getApiVersion(), raspberry.getKind(), raspberry.getMetadata().getName(), raspberry.getSpec().getIp(), raspberry.getSpec().getPort());

        RSocketRequester requester = getRSocketRequester(raspberry.getSpec().getIp(), raspberry.getSpec().getPort());
        String newRaspberry = requester
            .route("resource")
            .data(raspberry)
            .retrieveMono(String.class)
            .block();
        try {
            state.addResource(newRaspberry, false);
        } catch (IOException e) {
            raspberry.addError(e.getMessage());
            log.error("Unable to read resource received form Raspberry Pi: {}", e.getMessage(), e);
        }
    }

    private RSocketRequester getRSocketRequester(String ip, int port) {
        return RSocketRequester.builder()
            .rsocketStrategies(
                RSocketStrategies.builder().
                    decoder(new Jackson2JsonDecoder())
                    .encoder(new Jackson2JsonEncoder())
                    .build())
            .rsocketConnector(
                rSocketConnector ->
                    rSocketConnector.reconnect(Retry.fixedDelay(2, Duration.ofSeconds(2)))
            )
            .dataMimeType(MimeTypeUtils.APPLICATION_JSON)
            .tcp(ip, port);
    }
}
