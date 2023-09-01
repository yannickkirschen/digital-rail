package sh.yannick.rail.api.resource;

import lombok.Data;
import sh.yannick.state.Resource;

import java.util.List;

@Resource.BaseClass(apiVersion = "embedded.yannick.sh/v1alpha1", kind = "Raspberry")
public class Raspberry extends Resource<Raspberry.Spec, Raspberry.Status> {
    @Data
    @Resource.SpecDefinition(apiVersion = "embedded.yannick.sh/v1alpha1", kind = "Raspberry")
    public static class Spec {
        private String ip;
        private int port;
        private List<Pin> pins;
    }

    @Data
    @Resource.StatusDefinition(apiVersion = "embedded.yannick.sh/v1alpha1", kind = "Raspberry")
    public static class Status {
        private List<Pin> pins;
    }

    @Data
    public static class Pin {
        private int gpio;
        private String mode;
    }
}
