package sh.yannick.tools.math;

import lombok.Getter;
import lombok.Setter;

import java.util.*;

public class Graph<T extends Vertex> {
    @Getter
    private final Map<String, T> vertices = new HashMap<>();

    @Getter
    @Setter
    private Map<String, Set<String>> adjacencyList = new HashMap<>();

    public Graph() {
    }

    public void addVertex(T vertex) {
        vertices.put(vertex.getLabel(), vertex);
        adjacencyList.putIfAbsent(vertex.getLabel(), new HashSet<>());
    }

    public void addEdge(T vertex1, T vertex2) {
        adjacencyList.get(vertex1.getLabel()).add(vertex2.getLabel());
        adjacencyList.get(vertex2.getLabel()).add(vertex1.getLabel());
    }

    public T findVertex(String label) {
        return vertices.get(label);
    }

    public List<List<T>> findPaths(String from, String to, GraphWalkingDecision<T> decision) {
        DepthFirstSearch<T> search = new DepthFirstSearch<>(decision, vertices, adjacencyList);
        search.search(from, to);
        return search.getPaths().stream().map(path -> path.stream().map(vertices::get).toList()).toList();
    }
}
