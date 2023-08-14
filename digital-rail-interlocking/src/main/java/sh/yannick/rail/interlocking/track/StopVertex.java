package sh.yannick.rail.interlocking.track;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class StopVertex extends TrackVertex {
    private String pointsTo;

    public StopVertex(String label, String pointsTo) {
        super(label);
        this.pointsTo = pointsTo;
    }

    @Override
    public String toString() {
        return getLabel();
    }
}
