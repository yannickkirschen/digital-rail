package sh.yannick.rail.interlocking.switches;

import lombok.RequiredArgsConstructor;
import sh.yannick.rail.api.resource.Block;
import sh.yannick.rail.api.resource.Switch;
import sh.yannick.state.State;

import java.util.LinkedList;
import java.util.List;

@RequiredArgsConstructor
public class BasicSwitchTranslator implements SwitchTranslator {
    private final State state;

    @Override
    public List<Switch> translate(List<Block> path) {
        List<Switch> switches = new LinkedList<>();

        for (int i = 0; i < path.size(); i++) {
            Block current = path.get(i);
            Block previous = i > 0 ? path.get(i - 1) : null;
            Block next = i < path.size() - 1 ? path.get(i + 1) : null;

            if (current.getSpec().getBlockSwitch() != null) {
                Switch _switch = state.getResource("rail.yannick.sh/v1alpha1", "Switch", current.getSpec().getBlockSwitch().getName(), Switch.class).orElseThrow();

                if (previous != null) {
                    if (previous.getMetadata().getName().equals(current.getSpec().getBlockSwitch().getBase())) {
                        _switch.getSpec().setPosition(Switch.Position.BASE);
                    } else if (previous.getMetadata().getName().equals(current.getSpec().getBlockSwitch().getAlternate())) {
                        _switch.getSpec().setPosition(Switch.Position.ALTERNATE);
                    }
                }

                if (next != null) {
                    if (next.getMetadata().getName().equals(current.getSpec().getBlockSwitch().getBase())) {
                        _switch.getSpec().setPosition(Switch.Position.BASE);
                    } else if (next.getMetadata().getName().equals(current.getSpec().getBlockSwitch().getAlternate())) {
                        _switch.getSpec().setPosition(Switch.Position.ALTERNATE);
                    }
                }

                switches.add(_switch);
            }
        }

        return switches;
    }
}
