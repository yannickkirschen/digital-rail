package sh.yannick.rail.interlocking.signalling;

import lombok.RequiredArgsConstructor;
import sh.yannick.rail.api.resource.Block;
import sh.yannick.rail.api.resource.BlockStopPoint;
import sh.yannick.rail.api.resource.Signal;
import sh.yannick.state.State;

import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
public class SimpleSignallingSystem implements SignallingSystem {
    private static final String SIGNAL_API_VERSION = "rail.yannick.sh/v1alpha1";

    private final State state;

    @Override
    public List<Signal> translate(List<Block> path) {
        List<Signal> signals = new LinkedList<>();

        for (int i = 0; i < path.size(); i++) {
            Block current = path.get(i);
            Block next = i < path.size() - 1 ? path.get(i + 1) : null;

            if (!current.getSpec().getStopPoints().isEmpty()) {
                for (BlockStopPoint stop : current.getSpec().getStopPoints()) {
                    int value = 0;

                    if (next != null) {
                        if (stop.getTo().equals(next.getMetadata().getName())) {
                            value = 1;
                        }
                    }

                    Signal signal = state.getResource(SIGNAL_API_VERSION, "Signal", stop.getName(), Signal.class).orElseThrow(() -> new NoSuchElementException("%s/Signal %s not found.".formatted(SIGNAL_API_VERSION, stop.getName())));
                    signal.getSpec().setIndication(value == 0 ? Signal.Indication.STOP : Signal.Indication.CLEAR);
                    signal.getStatus().setSystemValue(value);

                    signals.add(signal);
                }
            }
        }

        return signals;
    }
}
