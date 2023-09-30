package sh.yannick.rail.interlocking;

import lombok.RequiredArgsConstructor;
import sh.yannick.rail.api.resource.Allocation;
import sh.yannick.rail.api.resource.Signal;
import sh.yannick.rail.api.resource.Switch;
import sh.yannick.state.State;

import java.util.LinkedList;
import java.util.List;

/**
 * The {@link AllocationTransaction} handles the allocation process in the "real world" after the path has been determined.
 * <p>
 * Here it gets really important: we have to keep a specific order when setting elements to the desired position! The
 * order is always:
 *  <ol>
 *      <li>switches</li>
 *      <li>flank protective signals</li>
 *      <li>signals on the path</li>
 *      <li>starting signal</li>
 *  </ol>
 * <p>
 * When using this class you have to make sure that {@link #signals} is an ordered list (e.g. {@link LinkedList})! The
 * transaction implementation of this class expects the first element of the list being the starting signal.  The order
 * of the other signals does not really matter. When setting the indications on the signals, we first iterate through
 * the list of signals following those steps:
 *  <ul>
 *      <li>extract starting signal</li>
 *      <li>extract all signals with indication "stop"</li>
 *      <li>ensure all signals desired to show "stop" already show stop (lock them afterward)</li>
 *      <li>iterate through the remaining list backwards and setting the desired indication (lock them afterward)</li>
 *      <li>set the indication of the starting signal at last (also lock it)</li>
 *  </ul>
 *
 * @author Yannick Kirschen
 * @since 1.0.0
 */
@RequiredArgsConstructor
public class AllocationTransaction {
    private final State state;

    private final Allocation allocation;
    private final List<Switch> switches;
    private final List<Signal> signals;

    public boolean commit() {
        return commitSwitches() && commitSignals();
    }

    public void rollback() {
        rollbackSwitches();
        rollbackSignals();
    }

    private boolean commitSwitches() {
        switches.forEach(state::addResource); // Update all switches

        List<String> errors = new LinkedList<>();
        for (Switch _switch : switches) {
            if (_switch.getErrors() != null) {
                errors.addAll(_switch.getErrors());
            }
        }

        if (!errors.isEmpty()) {
            allocation.addErrors(errors);
            return false;
        }

        return true;
    }

    private void rollbackSwitches() {
        for (Switch _switch : switches) {
            _switch.getSpec().setPosition(Switch.Position.BASE);
            state.addResource(_switch);
        }
    }

    private boolean commitSignals() {
        if (signals.isEmpty()) {
            throw new IllegalArgumentException("There are no signals involved in this allocation - this makes no sense.");
        }

        Signal startingSignal = signals.get(0);
        List<Signal> stops = signals.stream().filter(signal -> signal.getSpec().getIndication().equals(Signal.Indication.STOP)).toList();
        List<Signal> clears = signals.stream().filter(signal -> signal.getSpec().getIndication().equals(Signal.Indication.CLEAR)).toList();

        return commitSignals(stops) && commitSignals(clears) && commitSignals(List.of(startingSignal));
    }

    private boolean commitSignals(List<Signal> signals) {
        signals.forEach(state::addResource);

        List<String> errors = new LinkedList<>();
        for (Signal signal : signals) {
            if (signal.getErrors() != null) {
                errors.addAll(signal.getErrors());
            }
        }

        if (!errors.isEmpty()) {
            allocation.addErrors(errors);
            return false;
        }

        return true;
    }

    private void rollbackSignals() {
        for (Signal signal : signals) {
            signal.getSpec().setIndication(Signal.Indication.STOP);
            state.addResource(signal);
        }
    }
}
