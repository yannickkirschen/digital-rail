package sh.yannick.rail.concentrator;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;
import sh.yannick.state.Resource;
import sh.yannick.state.State;

import java.io.IOException;

@Controller
@RequiredArgsConstructor
public class ResourceController {
    private final State state;

    @MessageMapping("resource")
    public Mono<Resource<?, ?>> addResource(String resource) throws IOException {
        return Mono.just(state.addResource(resource));
    }
}
