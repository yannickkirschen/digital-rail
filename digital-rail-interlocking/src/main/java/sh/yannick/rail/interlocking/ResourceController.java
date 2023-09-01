package sh.yannick.rail.interlocking;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import sh.yannick.state.State;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class ResourceController {
    private final State state;

    @PostMapping(value = "/resource/yaml", consumes = "plain/text", produces = "plain/text")
    public String addYamlResource(@RequestBody String resource) throws IOException {
        return new ObjectMapper().writeValueAsString(state.addResource(resource));
    }
}
