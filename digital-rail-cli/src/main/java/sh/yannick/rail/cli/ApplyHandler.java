package sh.yannick.rail.cli;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import sh.yannick.state.Resource;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ApplyHandler {
    private final HttpClient client = new HttpClient();

    public void apply(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java -jar digital-rail-cli.jar apply <file or directory>");
            System.exit(1);
        }

        try {
            apply(args[1]);
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

    public void apply(String element) throws IOException {
        File fileOrDirectory = new File(element);
        if (fileOrDirectory.isDirectory()) {
            Map<File, String> files = new HashMap<>();
            for (File file : Objects.requireNonNull(fileOrDirectory.listFiles())) {
                JsonNode kindNode = new ObjectMapper(new YAMLFactory()).readTree(file).at("/kind");
                if (kindNode.isMissingNode()) {
                    throw new IOException(file + " is missing a kind");
                }

                String kind = kindNode.asText();
                files.put(file, kind);
            }

            Map<File, String> sortedFiles = MapUtil.sort(files, new ResourcePriorityComparator());
            for (Map.Entry<File, String> entry : sortedFiles.entrySet()) {
                applyFile(entry.getKey());
            }
        } else {
            applyFile(fileOrDirectory);
        }
        client.close();
    }

    private void applyFile(File file) {
        System.out.println("Applying " + file.getName());
        Resource<?, ?> resource = client.addResource(file);
        if (resource.getErrors() != null && !resource.getErrors().isEmpty()) {
            System.out.println("Error: " + resource.getErrors());
        }
    }
}
