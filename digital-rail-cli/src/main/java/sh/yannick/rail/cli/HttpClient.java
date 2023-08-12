package sh.yannick.rail.cli;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.net.URIBuilder;
import sh.yannick.rail.api.AllocationResponse;

import java.io.Closeable;
import java.io.IOException;
import java.net.URISyntaxException;

public class HttpClient implements Closeable {
    private final CloseableHttpClient client = HttpClients.createDefault();

    public AllocationResponse allocate(String from, String to) {
        try {
            URIBuilder uriBuilder = new URIBuilder("http://localhost:8080/allocate")
                .addParameter("from", from)
                .addParameter("to", to);
            HttpPost httpPost = new HttpPost(uriBuilder.build());

            return client.execute(httpPost, new AllocationResponseHandler());
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws IOException {
        client.close();
    }
}
