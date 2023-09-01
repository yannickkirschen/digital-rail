package sh.yannick.math;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
public class DepthFirstSearch<T extends Vertex> {
    private final List<String> visited = new LinkedList<>();
    private final LinkedList<String> currentPath = new LinkedList<>();

    @Getter
    private final List<List<String>> paths = new LinkedList<>();

    private final Map<String, T> vertices;
    private final Map<String, Set<String>> adjacencyList;
    private final GraphWalkingDecision<T> decision;

    public void search(String from, String to) {
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
                search(vertex, to);
            }
        }

        currentPath.removeLast();
        visited.remove(from);
    }
}
