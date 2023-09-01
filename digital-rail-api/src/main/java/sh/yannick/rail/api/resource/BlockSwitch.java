package sh.yannick.rail.api.resource;

import lombok.Data;

@Data
public class BlockSwitch {
    private String name;
    private String base;
    private String alternate;
}
