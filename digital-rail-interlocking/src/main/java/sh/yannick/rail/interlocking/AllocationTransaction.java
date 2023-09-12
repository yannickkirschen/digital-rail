package sh.yannick.rail.interlocking;

import lombok.RequiredArgsConstructor;
import sh.yannick.rail.api.resource.Allocation;
import sh.yannick.rail.api.resource.Signal;
import sh.yannick.rail.api.resource.Switch;
import sh.yannick.state.State;

import java.util.LinkedList;
import java.util.List;

@RequiredArgsConstructor
public class AllocationTransaction {
    private final State state;

    private final Allocation allocation;
    private final List<Switch> switches;
    private final List<Signal> signals;

    public boolean commit() {
        return commitSwitches() || commitSignals();
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
        signals.forEach(state::addResource); // Update all signals

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
