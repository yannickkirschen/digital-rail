package sh.yannick.rail.core.track;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SwitchVertex extends TrackVertex {
    private String baseVertex;
    private String alternateVertex;

    public SwitchVertex(String label, String baseVertex, String alternateVertex) {
        super(label);
        this.baseVertex = baseVertex;
        this.alternateVertex = alternateVertex;
    }

    @Override
    public String toString() {
        return getLabel();
    }
}
