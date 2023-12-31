package sh.yannick.rail.interlocking.listener;

import sh.yannick.math.DepthFirstSearch;
import sh.yannick.math.GraphWalkingDecision;
import sh.yannick.rail.api.resource.*;
import sh.yannick.rail.interlocking.AllocationTransaction;
import sh.yannick.rail.interlocking.signalling.SignallingSystem;
import sh.yannick.rail.interlocking.switches.BasicSwitchTranslator;
import sh.yannick.state.Listener;
import sh.yannick.state.Resource;
import sh.yannick.state.ResourceListener;
import sh.yannick.state.State;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

@Listener(apiVersion = "rail.yannick.sh/v1alpha1", kind = "Allocation")
public class AllocationListener implements ResourceListener<Allocation.Spec, Allocation.Status, Allocation> {
    private static final String API_VERSION = "rail.yannick.sh/v1alpha1";

    private State state;

    @Override
    public void onInit(State state) {
        this.state = state;
    }

    @Override
    public void onCreate(Allocation allocation) {
        Configuration configuration = state.getResource(API_VERSION, "Configuration", "configuration", Configuration.class).orElseThrow(() -> new NoSuchElementException("configuration/Configuration not found."));

        Optional<Graph> optionalGraph = state.getResource(API_VERSION, "Graph", allocation.getSpec().getGraph(), Graph.class);
        if (optionalGraph.isEmpty()) {
            allocation.addError("Allocation %s requires graph %s that does not exist.", allocation.getMetadata().getName(), allocation.getSpec().getGraph());
            return;
        }

        String fromName = allocation.getSpec().getFrom();
        String toName = allocation.getSpec().getTo();

        Optional<Block> optionalFrom = state.getResource(API_VERSION, "Block", fromName, Block.class);
        Optional<Block> optionalTo = state.getResource(API_VERSION, "Block", toName, Block.class);

        if (optionalFrom.isEmpty()) {
            allocation.addError("Resource Block %s not found for %s.", fromName, API_VERSION);
            return;
        }

        if (optionalTo.isEmpty()) {
            allocation.addError("Resource Block %s not found for %s.", toName, API_VERSION);
            return;
        }

        Allocation.Status status = new Allocation.Status();
        allocation.setStatus(status);

        status.setFrom(fromName);
        status.setTo(toName);
        status.setProgress(Allocation.Progress.CALCULATING);

        Resource<Graph.Spec, Graph.Status> graph = optionalGraph.get();
        DepthFirstSearch<BlockVertex> search = new DepthFirstSearch<>(graph.getStatus().getVertices(), graph.getStatus().getAdjacencyList(), new SwitchDecision());
        List<List<String>> allPaths = search.search(fromName, toName);
        List<List<Block>> paths = allPaths
            .stream()
            .map(path -> path
                .stream()
                .map(step -> {
                    Optional<Block> block = state.getResource(API_VERSION, "Block", step, Block.class);
                    if (block.isEmpty()) {
                        throw new IllegalArgumentException("Block " + step + " should exist but doesn't.");
                    }
                    return block.get();
                }).toList()).toList();

        status.setAllPaths(allPaths);

        // TODO: find best path
        List<Block> path = paths.get(0);
        status.setChosenPath(path.stream().map(block -> block.getMetadata().getName()).toList());
        status.setProgress(Allocation.Progress.ALLOCATING);

        for (Block block : path) {
            if (block.getStatus().isLocked()) {
                allocation.addError("Block %s is already blocked.", block.getMetadata().getName());
                return;
            }
        }

        List<Switch> switches = new BasicSwitchTranslator(state).translate(path);
        List<Signal> signals = SignallingSystem.get(state, configuration.getSpec().getSignallingSystem()).translate(path);

        for (Switch _switch : switches) {
            System.out.println(_switch.getMetadata().getName() + " " + _switch.getStatus().isLocked());
            if (_switch.getStatus().isLocked()) {
                allocation.addError("Switch %s is already locked.", _switch.getMetadata().getName());
                return;
            }
        }

        for (Signal signal : signals) {
            System.out.println(signal.getMetadata().getName() + " " + signal.getStatus().isLocked());
            if (signal.getStatus().isLocked()) {
                allocation.addError("Signal %s is already locked.", signal.getMetadata().getName());
                return;
            }
        }

        AllocationTransaction transaction = new AllocationTransaction(state, allocation, switches, signals);
        if (transaction.commit()) {
            path.forEach(block -> block.getStatus().setLocked(true));
            switches.forEach(_switch -> _switch.getStatus().setLocked(true));
            signals.forEach(signal -> signal.getStatus().setLocked(true));
            status.setProgress(Allocation.Progress.LOCKED);
        } else {
            transaction.rollback();
        }
    }

    private static class SwitchDecision implements GraphWalkingDecision<BlockVertex> {
        @Override
        public boolean shouldWalk(BlockVertex from, BlockVertex to, BlockVertex via) {
            Map<String, String> prohibits = via.getBlock().getSpec().getProhibits();
            return !prohibits.containsKey(from.getLabel()) || !prohibits.get(from.getLabel()).equals(to.getLabel());
        }
    }
}
