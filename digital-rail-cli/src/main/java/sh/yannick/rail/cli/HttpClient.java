package sh.yannick.rail.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.net.URIBuilder;
import sh.yannick.state.Resource;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;

public class HttpClient implements Closeable {
    private final CloseableHttpClient client = HttpClients.createDefault();

    public Resource<?, ?> addResource(File file) {
        try {
            URIBuilder uriBuilder = new URIBuilder("http://localhost:8080/resource/yaml");
            HttpPost httpPost = new HttpPost(uriBuilder.build());
            httpPost.setEntity(new StringEntity(Files.readString(file.toPath())));
            httpPost.setHeader("Content-Type", "plain/text");

            return client.execute(httpPost, response -> {
                if (response.getCode() != 200) {
                    throw new RuntimeException("Error: " + response.getCode());
                }

                return new ObjectMapper().readValue(response.getEntity().getContent(), Resource.class);
            });
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws IOException {
        client.close();
    }
}
