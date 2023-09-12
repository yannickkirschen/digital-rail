package sh.yannick.rail.interlocking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import sh.yannick.state.State;

import java.io.IOException;

@Slf4j
@SpringBootApplication
public class Main {
    public static void main(String[] args) throws IOException {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    public State getState() {
        // Spring detects that State implements Closeable and will call close() on shutdown.
        return State
            .builder()
            .withName("rail-concentrator")
            .withPackages("sh.yannick.rail.api", "sh.yannick.rail.interlocking.listener")
            .build()
            .initializeListeners();
    }
}
