package sh.yannick.math;

import java.util.*;

/**
 * Represents a graph data structure using an adjacency list representation. This class provides
 * methods to create, manipulate, and analyze a graph, as well as find all paths between vertices.
 * <p>
 * The graph can be either directed or undirected, and it stores vertices of type T.
 *
 * @param <T> the type of data stored in each vertex of the graph
 * @author Yannick Kirschen
 * @since 1.0.0
 */
public class Graph<T extends Vertex> {
    private final Map<String, T> vertices = new HashMap<>();
    private final Map<String, Set<String>> adjacencyList = new HashMap<>();

    /**
     * Adds a vertex to the graph.
     *
     * @param vertex vertex to add
     */
    public void addVertex(T vertex) {
        vertices.put(vertex.getLabel(), vertex);
        adjacencyList.putIfAbsent(vertex.getLabel(), new HashSet<>());
    }

    /**
     * Adds an edge between two vertices. In fact, this creates a bidirectional edge between those vertices.
     *
     * @param vertex1 first vertex
     * @param vertex2 second vertex
     */
    public void addEdge(T vertex1, T vertex2) {
        adjacencyList.get(vertex1.getLabel()).add(vertex2.getLabel());
        adjacencyList.get(vertex2.getLabel()).add(vertex1.getLabel());
    }

    /**
     * Finds all possible path between two vertices.
     *
     * @param from     start vertex
     * @param to       destination vertex
     * @param decision optional decision when multiple ways can be taken
     * @return all possible paths
     */
    public List<List<T>> findPaths(String from, String to, GraphWalkingDecision<T> decision) {
        return new DepthFirstSearch<>(vertices, adjacencyList, decision)
            .search(from, to)
            .stream()
            .map(path -> path
                .stream()
                .map(vertices::get)
                .toList()
            )
            .toList();
    }
}
