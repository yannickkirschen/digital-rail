package sh.yannick.rail.interlocking.switches;

import lombok.RequiredArgsConstructor;
import sh.yannick.rail.api.resource.Block;
import sh.yannick.rail.api.resource.Switch;
import sh.yannick.state.State;

import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
public class BasicSwitchTranslator implements SwitchTranslator {
    private static final String SWITCH_API_VERSION = "rail.yannick.sh/v1alpha1";

    private final State state;

    @Override
    public List<Switch> translate(List<Block> path) {
        List<Switch> switches = new LinkedList<>();

        for (int i = 0; i < path.size(); i++) {
            Block current = path.get(i);
            Block previous = i > 0 ? path.get(i - 1) : null;
            Block next = i < path.size() - 1 ? path.get(i + 1) : null;

            if (current.getSpec().getBlockSwitch() != null) {
                Switch _switch = state.getResource(SWITCH_API_VERSION, "Switch", current.getSpec().getBlockSwitch().getName(), Switch.class).orElseThrow(() -> new NoSuchElementException("%s/Switch %s not found.".formatted(SWITCH_API_VERSION, current.getSpec().getBlockSwitch().getName())));

                setFor(previous, current, _switch);
                setFor(next, current, _switch);

                switches.add(_switch);
            }
        }

        return switches;
    }

    private void setFor(Block block, Block current, Switch _switch) {
        if (block != null) {
            if (block.getMetadata().getName().equals(current.getSpec().getBlockSwitch().getBase())) {
                _switch.getSpec().setPosition(Switch.Position.BASE);
            } else if (block.getMetadata().getName().equals(current.getSpec().getBlockSwitch().getAlternate())) {
                _switch.getSpec().setPosition(Switch.Position.ALTERNATE);
            }
        }
    }
}
