package sh.yannick.rail.interlocking.track;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sh.yannick.tools.math.Vertex;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "kind")
@JsonSubTypes({
    @JsonSubTypes.Type(value = StopVertex.class, name = "Stop"),
    @JsonSubTypes.Type(value = SwitchVertex.class, name = "Switch")
})
public class TrackVertex implements Vertex {
    @Getter
    private final Map<String, String> prohibits = new HashMap<>();

    @Getter
    private String label;

    @Getter
    @Setter
    private boolean locked;

    public TrackVertex(String label) {
        this.label = label;
    }

    public void prohibit(TrackVertex from, TrackVertex to) {
        prohibits.put(from.label, to.label);
        prohibits.put(to.label, from.label);
    }

    @Override
    public String toString() {
        return label;
    }
}
