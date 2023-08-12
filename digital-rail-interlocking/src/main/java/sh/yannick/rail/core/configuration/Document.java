package sh.yannick.rail.core.configuration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import sh.yannick.rail.core.track.TrackVertex;
import sh.yannick.tools.math.Graph;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Document {
    private Graph<TrackVertex> graph;
    private List<InventoryElement> inventory;
}
