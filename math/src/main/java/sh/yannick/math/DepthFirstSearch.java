package sh.yannick.math;

import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implements the <a href="https://en.wikipedia.org/wiki/Depth-first_search">Depth first search</a>.
 *
 * @param <T> the type of data stored in each vertex of the graph
 * @author Yannick Kirschen
 * @since 1.0.0
 */
@RequiredArgsConstructor
public class DepthFirstSearch<T extends Vertex> {
    private final List<String> visited = new LinkedList<>();
    private final LinkedList<String> currentPath = new LinkedList<>();
    private final List<List<String>> paths = new LinkedList<>();

    private final Map<String, T> vertices;
    private final Map<String, Set<String>> adjacencyList;
    private final GraphWalkingDecision<T> decision;

    /**
     * Finds all possible path between two vertices.
     *
     * @param from start vertex
     * @param to   destination vertex
     * @return all possible paths
     */
    public List<List<String>> search(String from, String to) {
        depthFirstSearch(from, to);
        return paths;
    }

    private void depthFirstSearch(String from, String to) {
        if (visited.contains(from)) {
            return;
        }

        visited.add(from);
        currentPath.addLast(from);
        if (from.equals(to)) {
            paths.add(new LinkedList<>(currentPath));
            visited.remove(from);
            currentPath.removeLast();
            return;
        }

        for (String vertex : adjacencyList.get(from)) {
            if (decision.shouldWalk(vertices.get(from), vertices.get(to), vertices.get(vertex))) {
                depthFirstSearch(vertex, to);
            }
        }

        currentPath.removeLast();
        visited.remove(from);
    }
}
