package sh.yannick.state;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Metadata {
    private String name;
    private Map<String, String> labels = new HashMap<>();

    public Metadata(String name) {
        this.name = name;
    }
}
