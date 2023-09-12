package sh.yannick.rail.api.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import sh.yannick.state.Resource;

@Resource.BaseClass(apiVersion = "rail.yannick.sh/v1alpha1", kind = "Configuration")
public class Configuration extends Resource<Configuration.Spec, Configuration.Status> {
    @Data
    @Resource.SpecDefinition(apiVersion = "rail.yannick.sh/v1alpha1", kind = "Configuration")
    public static class Spec {
        @JsonProperty("signalling-system")
        private String signallingSystem;
    }

    @Data
    @Resource.StatusDefinition(apiVersion = "rail.yannick.sh/v1alpha1", kind = "Configuration")
    public static class Status {
    }
}

