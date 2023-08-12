package sh.yannick.rail.core.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.InputStream;

@RequiredArgsConstructor
public class DocumentReader {
    private final InputStream in;

    public Document read() throws IOException {
        return new ObjectMapper().readValue(in, Document.class);
    }
}
