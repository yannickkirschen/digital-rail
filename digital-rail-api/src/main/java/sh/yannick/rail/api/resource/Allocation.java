package sh.yannick.rail.api.resource;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import sh.yannick.state.Resource;

import java.util.List;

@Resource.BaseClass(apiVersion = "rail.yannick.sh/v1alpha1", kind = "Allocation")
public class Allocation extends Resource<Allocation.Spec, Allocation.Status> {
    @RequiredArgsConstructor
    public enum Progress {
        CALCULATING("calculating"),
        ALLOCATING("allocating"),
        LOCKED("locked"),
        RELEASING("releasing"),
        RELEASED("released");

        @JsonValue
        private final String displayName;
    }

    @Data
    @Resource.SpecDefinition(apiVersion = "rail.yannick.sh/v1alpha1", kind = "Allocation")
    public static class Spec {
        private String from;
        private String to;
        private String graph;
    }

    @Data
    @Resource.StatusDefinition(apiVersion = "rail.yannick.sh/v1alpha1", kind = "Allocation")
    public static class Status {
        private String from;
        private String to;
        private List<List<String>> allPaths;
        private List<String> chosenPath;
        private Progress progress;
    }
}
