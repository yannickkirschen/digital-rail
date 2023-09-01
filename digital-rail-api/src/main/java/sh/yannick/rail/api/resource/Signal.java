package sh.yannick.rail.api.resource;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Data;
import lombok.Getter;
import sh.yannick.state.Resource;

@Resource.BaseClass(apiVersion = "rail.yannick.sh/v1alpha1", kind = "Signal")
public class Signal extends Resource<Signal.Spec, Signal.Status> {
    public enum Indication {
        STOP("stop"),
        CLEAR("clear");

        @Getter
        @JsonValue
        private final String indication;

        Indication(String indication) {
            this.indication = indication;
        }
    }

    @Data
    @Resource.SpecDefinition(apiVersion = "rail.yannick.sh/v1alpha1", kind = "Signal")
    public static class Spec {
        private Indication indication;
    }

    @Data
    @Resource.StatusDefinition(apiVersion = "rail.yannick.sh/v1alpha1", kind = "Signal")
    public static class Status {
        private int systemValue;
        private boolean locked;
    }
}
