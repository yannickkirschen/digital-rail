package sh.yannick.rail.cli;

import sh.yannick.rail.api.AllocationResponse;

import java.io.Closeable;
import java.io.Console;
import java.io.IOException;
import java.net.URISyntaxException;

public class Cli implements Closeable {
    private final HttpClient client = new HttpClient();

    public void run() throws URISyntaxException {
        Console console = System.console();

        while (true) {
            System.out.println();
            String operation = console.readLine("> ");

            if (operation == null) {
                continue;
            }

            if (operation.equals("q")) {
                break;
            }

            String[] operationParts = operation.split("\\.");
            if (operationParts.length != 2) {
                System.out.println("Invalid operation.");
                continue;
            }

            String from = operationParts[0];
            String to = operationParts[1];

            AllocationResponse allocationResponse = client.allocate(from, to);
            if (allocationResponse.error() != null) {
                System.out.println("Error: " + allocationResponse.error());
                continue;
            }

            System.out.println("Allocated: " + allocationResponse.path());
        }
    }

    @Override
    public void close() throws IOException {
        client.close();
    }
}
