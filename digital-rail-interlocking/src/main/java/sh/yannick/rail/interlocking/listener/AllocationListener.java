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
        Configuration configuration = state.getResource(API_VERSION, "Configuration", "configuration", Configuration.class).orElseThrow();

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

        if (allocation.getStatus() == null) {
            allocation.setStatus(new Allocation.Status());
        }

        allocation.getStatus().setFrom(fromName);
        allocation.getStatus().setTo(toName);
        allocation.getStatus().setProgress(Allocation.Progress.CALCULATING);

        Resource<Graph.Spec, Graph.Status> graph = optionalGraph.get();
        DepthFirstSearch<BlockVertex> search = new DepthFirstSearch<>(graph.getStatus().getVertices(), graph.getStatus().getAdjacencyList(), new SwitchDecision());
        search.search(fromName, toName);
        List<List<Block>> paths = search
            .getPaths()
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

        allocation.getStatus().setAllPaths(search.getPaths());

        // TODO: find best path
        List<Block> path = paths.get(0);
        allocation.getStatus().setChosenPath(path.stream().map(block -> block.getMetadata().getName()).toList());
        allocation.getStatus().setProgress(Allocation.Progress.ALLOCATING);

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
            switches.forEach(_switch -> _switch.getStatus().setLocked(true));
            signals.forEach(signal -> signal.getStatus().setLocked(true));
            path.forEach(block -> block.getStatus().setLocked(true));
            allocation.getStatus().setProgress(Allocation.Progress.LOCKED);
        } else {
            transaction.rollback();
        }
    }

    @Override
    public void onUpdate(Allocation allocation) {
        if (!allocation.getStatus().getProgress().equals(Allocation.Progress.RELEASED)) {
            allocation.addError("Allocation %s is not released yet and thus cannot be updated.", allocation.getMetadata().getName());
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
