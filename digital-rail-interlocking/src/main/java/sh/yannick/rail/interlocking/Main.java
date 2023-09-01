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
//        State state = SpringApplication.run(Main.class, args).getBean(State.class);
//
//        state.addResource(new File("examples/rpi.yaml"));
//
//        state.addResource(new File("examples/block-1.yaml"));
//        state.addResource(new File("examples/block-2.yaml"));
//        state.addResource(new File("examples/block-3.yaml"));
//        state.addResource(new File("examples/block-4.yaml"));
//        state.addResource(new File("examples/block-5.yaml"));
//        state.addResource(new File("examples/block-6.yaml"));
//
//        state.addResource(new File("examples/signal-A.yaml"));
//        state.addResource(new File("examples/signal-F.yaml"));
//        state.addResource(new File("examples/signal-N1.yaml"));
//        state.addResource(new File("examples/signal-N2.yaml"));
//        state.addResource(new File("examples/signal-P1.yaml"));
//        state.addResource(new File("examples/signal-P2.yaml"));
//
//        state.addResource(new File("examples/switch-SW1.yaml"));
//        state.addResource(new File("examples/switch-SW2.yaml"));
//
//        state.addResource(new File("examples/graph-1.yaml"));
//
//        Allocation allocation = state.addResource(new File("examples/allocation-1.yaml"));
//        log.info("{}", allocation.getErrors());
    }

    @Bean
    public State getState() {
        // Spring detects that State implements Closeable and will call close() on shutdown.
        return State
            .getEmpty()
            .withName("rail-concentrator")
            .withPackages("sh.yannick.rail.api", "sh.yannick.rail.interlocking.listener")
            .initializeListeners();
    }
}
