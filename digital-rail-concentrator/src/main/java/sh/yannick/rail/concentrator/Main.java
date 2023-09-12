package sh.yannick.rail.concentrator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import sh.yannick.state.State;

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    public State getState() {
        // Spring detects that State implements Closeable and will call close() on shutdown.
        return State
            .builder()
            .withName("rail-concentrator")
            .withPackages("sh.yannick.rail.api", "sh.yannick.rail.concentrator")
            .build()
            .initializeListeners();
    }
}
