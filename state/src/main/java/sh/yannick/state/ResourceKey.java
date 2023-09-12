package sh.yannick.state;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class ResourceKey {
    private final String apiVersion;
    private final String kind;
    private final String name;
}
