package sh.yannick.rail.api.resource;

import lombok.Data;
import sh.yannick.state.Resource;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Resource.BaseClass(apiVersion = "rail.yannick.sh/v1alpha1", kind = "Graph")
public class Graph extends Resource<Graph.Spec, Graph.Status> {
    @Data
    @Resource.SpecDefinition(apiVersion = "rail.yannick.sh/v1alpha1", kind = "Graph")
    public static class Spec {
        private Set<String> verticesFromRef;
        private Map<String, Set<String>> adjacencyList;
    }

    @Data
    @Resource.StatusDefinition(apiVersion = "rail.yannick.sh/v1alpha1", kind = "Graph")
    public static class Status {
        private Map<String, BlockVertex> vertices = new HashMap<>();
        private Map<String, Set<String>> adjacencyList = new HashMap<>();
    }
}
