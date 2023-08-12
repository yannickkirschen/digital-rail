package sh.yannick.rail.cli;

import java.io.IOException;
import java.net.URISyntaxException;

public class Main {
    public static void main(String[] args) throws URISyntaxException {
        try (Cli cli = new Cli()) {
            cli.run();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
