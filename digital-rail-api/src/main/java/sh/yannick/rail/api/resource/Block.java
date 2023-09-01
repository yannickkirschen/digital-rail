package sh.yannick.rail.api.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import sh.yannick.state.Resource;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Resource.BaseClass(apiVersion = "rail.yannick.sh/v1alpha1", kind = "Block")
public class Block extends Resource<Block.Spec, Block.Status> {
    @Data
    @Resource.SpecDefinition(apiVersion = "rail.yannick.sh/v1alpha1", kind = "Block")
    public static class Spec {
        private final Map<String, String> prohibits = new HashMap<>();
        private boolean locked;
        private List<BlockStopPoint> stopPoints = new LinkedList<>();

        @JsonProperty("switch")
        private BlockSwitch blockSwitch;
    }

    @Data
    @Resource.StatusDefinition(apiVersion = "rail.yannick.sh/v1alpha1", kind = "Block")
    public static class Status {
        private boolean locked;
    }
}
