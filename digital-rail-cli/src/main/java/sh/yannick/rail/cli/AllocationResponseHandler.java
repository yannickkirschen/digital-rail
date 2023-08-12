package sh.yannick.rail.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import sh.yannick.rail.api.AllocationResponse;

import java.io.IOException;

public class AllocationResponseHandler implements HttpClientResponseHandler<AllocationResponse> {
    @Override
    public AllocationResponse handleResponse(ClassicHttpResponse response) throws IOException {
        if (response.getCode() == 200) {
            return new ObjectMapper().readValue(response.getEntity().getContent(), AllocationResponse.class);
        }

        return new AllocationResponse(null, "Allocation response code: " + response.getCode());
    }
}
