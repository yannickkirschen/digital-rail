package sh.yannick.rail.api.resource;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Data;
import lombok.Getter;
import sh.yannick.state.Resource;

@Resource.BaseClass(apiVersion = "rail.yannick.sh/v1alpha1", kind = "Switch")
public class Switch extends Resource<Switch.Spec, Switch.Status> {
    public enum Position {
        BASE("base"),
        ALTERNATE("alternate");

        @Getter
        @JsonValue
        private final String position;

        Position(String position) {
            this.position = position;
        }
    }

    @Data
    @SpecDefinition(apiVersion = "rail.yannick.sh/v1alpha1", kind = "Switch")
    public static class Spec {
        private Position position;
    }

    @Data
    @StatusDefinition(apiVersion = "rail.yannick.sh/v1alpha1", kind = "Switch")
    public static class Status {
        private Position position;
        private boolean locked;
    }
}
