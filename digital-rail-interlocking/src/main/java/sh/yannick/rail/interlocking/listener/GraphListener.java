package sh.yannick.rail.interlocking.listener;

import sh.yannick.rail.api.resource.Block;
import sh.yannick.rail.api.resource.BlockVertex;
import sh.yannick.rail.api.resource.Graph;
import sh.yannick.state.Listener;
import sh.yannick.state.ResourceListener;
import sh.yannick.state.State;

import java.util.NoSuchElementException;
import java.util.Set;

@Listener(apiVersion = "rail.yannick.sh/v1alpha1", kind = "Graph")
public class GraphListener implements ResourceListener<Graph.Spec, Graph.Status, Graph> {
    private static final String BLOCK_API_VERSION = "rail.yannick.sh/v1alpha1";

    private State state;

    @Override
    public void onInit(State state) {
        this.state = state;
    }

    @Override
    public void onCreate(Graph graph) {
        if (graph.getStatus() == null) {
            graph.setStatus(new Graph.Status());
        }

        for (String vertex : graph.getSpec().getVerticesFromRef()) {
            Block block = state.getResource(BLOCK_API_VERSION, "Block", vertex, Block.class).orElseThrow(() -> new NoSuchElementException("%s/%s not found.".formatted(BLOCK_API_VERSION, vertex)));

            Set<String> adjacency = graph.getSpec().getAdjacencyList().get(vertex);
            if (adjacency == null) {
                graph.addError("Vertex %s not in adjacency list.", vertex);
                return;
            }

            graph.getStatus().getVertices().put(vertex, new BlockVertex(block));
            graph.getStatus().getAdjacencyList().put(vertex, adjacency);
        }
    }
}
