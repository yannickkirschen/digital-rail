package sh.yannick.rail.interlocking.listener;

import lombok.extern.slf4j.Slf4j;
import sh.yannick.rail.api.resource.Raspberry;
import sh.yannick.rail.api.resource.Switch;
import sh.yannick.state.Listener;
import sh.yannick.state.ResourceListener;
import sh.yannick.state.State;

import java.util.NoSuchElementException;

@Slf4j
@Listener(apiVersion = "rail.yannick.sh/v1alpha1", kind = "Switch")
public class RaspberrySwitchListener implements ResourceListener<Switch.Spec, Switch.Status, Switch> {
    private static final String RASPBERRY_API_VERSION = "embedded.yannick.sh/v1alpha1";

    private State state;

    @Override
    public void onInit(State state) {
        this.state = state;
    }

    @Override
    public void onCreate(Switch _switch) {
        Switch.Status status = new Switch.Status();
        status.setPosition(Switch.Position.BASE);
        status.setLocked(false);
        _switch.setStatus(status);

        state.addResource(_switch); // Triggers onUpdate
    }

    @Override
    public void onUpdate(Switch _switch) {
        log.info("Updating switch {} to {}", _switch.getMetadata().getName(), _switch.getStatus().getPosition());

        String rpi = _switch.getMetadata().getLabels().get("rail.yannick.sh/raspberry");
        Raspberry raspberry = state.getResource(RASPBERRY_API_VERSION, "Raspberry", rpi, Raspberry.class).orElseThrow(() -> new NoSuchElementException("%s/Raspberry %s not found.".formatted(RASPBERRY_API_VERSION, rpi)));

        Switch.Position position = _switch.getSpec().getPosition();

        int alternatePin = Integer.parseInt(_switch.getMetadata().getLabels().get("rail.yannick.sh/raspberry-alternate-pin"));

        for (Raspberry.Pin pin : raspberry.getSpec().getPins()) {
            if (pin.getGpio() == alternatePin && position == Switch.Position.BASE) {
                pin.setMode("off");
            } else if (pin.getGpio() == alternatePin && position == Switch.Position.ALTERNATE) {
                pin.setMode("on");
            }
        }

        state.addResource(raspberry);
    }
}
