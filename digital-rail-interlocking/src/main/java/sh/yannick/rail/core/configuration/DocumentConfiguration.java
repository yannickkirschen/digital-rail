package sh.yannick.rail.core.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import sh.yannick.rail.core.track.TrackVertex;
import sh.yannick.tools.math.Graph;

import java.io.FileInputStream;
import java.io.IOException;

@Component
public class DocumentConfiguration {
    private final Document document;

    public DocumentConfiguration(@Value("${document.path}") String path) throws IOException {
        this.document = new DocumentReader(new FileInputStream(path)).read();
    }

    @Bean
    public Document document() {
        return document;
    }

    @Bean
    public Graph<TrackVertex> graph() {
        return document.getGraph();
    }
}
