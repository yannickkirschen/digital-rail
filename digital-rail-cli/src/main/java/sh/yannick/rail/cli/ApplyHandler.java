package sh.yannick.rail.cli;

import sh.yannick.state.Resource;

import java.io.File;
import java.io.IOException;

public class ApplyHandler {
    private final HttpClient client = new HttpClient();

    public void apply(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java -jar digital-rail-cli.jar apply <file>");
            System.exit(1);
        }

        try {
            apply(args[1]);
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

    public void apply(String file) throws IOException {
        Resource<?, ?> resource = client.addResource(new File(file));
        if (resource.getErrors() != null && !resource.getErrors().isEmpty()) {
            System.out.println("Error: " + resource.getErrors());
        }
        client.close();
    }
}
