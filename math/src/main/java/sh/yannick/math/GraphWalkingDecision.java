package sh.yannick.math;

/**
 * By using a {@link GraphWalkingDecision} it is possible to block walking specific paths.
 *
 * @param <T> the type of data stored in each vertex of the graph
 * @author Yannick Kirschen
 * @since 1.0.0
 */
@FunctionalInterface
public interface GraphWalkingDecision<T extends Vertex> {
    /**
     * Checks whether the path should be walked or not.
     *
     * @param from start vertex of the walk
     * @param to   end vertex of the walk
     * @param via  middle vertex to walk over
     * @return <code>true</code> if the path should be walked, otherwise <code>false</code>.
     */
    boolean shouldWalk(T from, T to, T via);
}
