package sh.yannick.state;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class SpecKey {
    private final String apiVersion;
    private final String kind;
}
