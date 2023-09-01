package sh.yannick.rail.concentrator;

import lombok.extern.slf4j.Slf4j;
import sh.yannick.rail.api.resource.Raspberry;
import sh.yannick.rail.gpio.DefaultGpio;
import sh.yannick.rail.gpio.Gpio;
import sh.yannick.state.Listener;
import sh.yannick.state.ResourceListener;
import sh.yannick.state.State;

@Slf4j
@Listener(apiVersion = "embedded.yannick.sh/v1alpha1", kind = "Raspberry")
public class RaspberryListener implements ResourceListener<Raspberry.Spec, Raspberry.Status, Raspberry> {
    private final Gpio gpio = new DefaultGpio();
    private State state;

    @Override
    public void onInit(State state) {
        this.state = state;
    }

    @Override
    public void onCreate(Raspberry raspberry) {
        raspberry.setStatus(new Raspberry.Status());
        raspberry.getStatus().setPins(raspberry.getSpec().getPins());
        state.addResource(raspberry); // Triggers onUpdate
    }

    @Override
    public void onUpdate(Raspberry raspberry) {
        for (Raspberry.Pin pin : raspberry.getSpec().getPins()) {
            gpio.setPin(pin.getGpio(), pin.getMode());
            log.info("Set pin {} to mode {}", pin.getGpio(), pin.getMode());
        }

        for (Integer pin : gpio.usedPins()) {
            if (raspberry.getSpec().getPins().stream().noneMatch(p -> p.getGpio() == pin)) {
                gpio.lowerPin(pin);
                log.info("Lowered pin {}", pin);
            }
        }

        // TODO: find out why status is null when executing second, third, ... time
        raspberry.getStatus().setPins(raspberry.getSpec().getPins());
    }

    @Override
    public void onDelete(Raspberry raspberry) {
        for (Raspberry.Pin pin : raspberry.getSpec().getPins()) {
            gpio.disablePin(pin.getGpio());
            log.info("Disabled pin {}", pin.getGpio());
        }
    }
}
