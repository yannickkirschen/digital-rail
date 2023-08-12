package sh.yannick.rail.core;

import sh.yannick.rail.core.track.StopVertex;
import sh.yannick.rail.core.track.SwitchVertex;
import sh.yannick.rail.core.track.TrackVertex;
import sh.yannick.tools.math.Graph;

public class DevInit {
    private static final TrackVertex toLeft = new TrackVertex("toLeft");
    private static final TrackVertex toRight = new TrackVertex("toRight");
    private static final StopVertex sigA = new StopVertex("A", "SW1");
    private static final StopVertex sigF = new StopVertex("F", "SW2");
    private static final StopVertex sigN1 = new StopVertex("N1", "SW1");
    private static final StopVertex sigN2 = new StopVertex("N2", "SW1");
    private static final StopVertex sigP1 = new StopVertex("P1", "SW2");
    private static final StopVertex sigP2 = new StopVertex("P2", "SW2");
    private static final SwitchVertex sw1 = new SwitchVertex("SW1", "N1", "N2");
    private static final SwitchVertex sw2 = new SwitchVertex("SW2", "P1", "P2");

    static {
        sw1.prohibit(sigN1, sigN2);
        sw2.prohibit(sigP1, sigP2);
    }

    public static Graph<TrackVertex> getGraph() {
        Graph<TrackVertex> graph = new Graph<>();

        graph.addVertex(toLeft);
        graph.addVertex(toRight);
        graph.addVertex(sigA);
        graph.addVertex(sigF);
        graph.addVertex(sigN1);
        graph.addVertex(sigN2);
        graph.addVertex(sigP1);
        graph.addVertex(sigP2);
        graph.addVertex(sw1);
        graph.addVertex(sw2);

        graph.addEdge(toLeft, sigA);
        graph.addEdge(sigA, sw1);
        graph.addEdge(sw1, sigN1);
        graph.addEdge(sw1, sigN2);
        graph.addEdge(sigN1, sigP1);
        graph.addEdge(sigN2, sigP2);
        graph.addEdge(sigP1, sw2);
        graph.addEdge(sigP2, sw2);
        graph.addEdge(sw2, sigF);
        graph.addEdge(sigF, toRight);

        return graph;
    }
}

