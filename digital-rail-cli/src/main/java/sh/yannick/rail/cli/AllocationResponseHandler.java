package sh.yannick.rail.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import sh.yannick.rail.api.AllocationApiResponse;

import java.io.IOException;

public class AllocationResponseHandler implements HttpClientResponseHandler<AllocationApiResponse> {
    @Override
    public AllocationApiResponse handleResponse(ClassicHttpResponse response) throws IOException {
        if (response.getCode() == 200) {
            return new ObjectMapper().readValue(response.getEntity().getContent(), AllocationApiResponse.class);
        }

        return new AllocationApiResponse(null, "Allocation response code: " + response.getCode());
    }
}
