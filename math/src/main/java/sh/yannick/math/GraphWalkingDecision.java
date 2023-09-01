package sh.yannick.math;

@FunctionalInterface
public interface GraphWalkingDecision<T extends Vertex> {
    boolean shouldWalk(T from, T to, T via);
}
