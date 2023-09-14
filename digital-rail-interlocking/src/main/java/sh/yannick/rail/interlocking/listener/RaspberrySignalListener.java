package sh.yannick.rail.interlocking.listener;

import lombok.extern.slf4j.Slf4j;
import sh.yannick.rail.api.resource.Raspberry;
import sh.yannick.rail.api.resource.Signal;
import sh.yannick.state.Listener;
import sh.yannick.state.ResourceListener;
import sh.yannick.state.State;

import java.util.NoSuchElementException;

@Slf4j
@Listener(apiVersion = "rail.yannick.sh/v1alpha1", kind = "Signal")
public class RaspberrySignalListener implements ResourceListener<Signal.Spec, Signal.Status, Signal> {
    private static final String RASPBERRY_API_VERSION = "embedded.yannick.sh/v1alpha1";

    private State state;

    @Override
    public void onInit(State state) {
        this.state = state;
    }

    @Override
    public void onCreate(Signal signal) {
        Signal.Status status = new Signal.Status();
        status.setSystemValue(0);
        status.setLocked(false);
        signal.setStatus(status);

        state.addResource(signal); // Triggers onUpdate
    }

    @Override
    public void onUpdate(Signal signal) {
        log.info("Updating signal {} to {}", signal.getMetadata().getName(), signal.getStatus().getSystemValue());

        String rpi = signal.getMetadata().getLabels().get("rail.yannick.sh/raspberry");
        Raspberry raspberry = state.getResource(RASPBERRY_API_VERSION, "Raspberry", rpi, Raspberry.class).orElseThrow(() -> new NoSuchElementException("%s/Raspberry %s not found.".formatted(RASPBERRY_API_VERSION, rpi)));

        int systemValue = signal.getStatus().getSystemValue();

        int stopPin = Integer.parseInt(signal.getMetadata().getLabels().get("rail.yannick.sh/raspberry-stop-pin"));
        int clearPin = Integer.parseInt(signal.getMetadata().getLabels().get("rail.yannick.sh/raspberry-clear-pin"));

        for (Raspberry.Pin pin : raspberry.getSpec().getPins()) {
            if (pin.getGpio() == stopPin && systemValue == 0) {
                pin.setMode("on");
            } else if (pin.getGpio() == stopPin && systemValue == 1) {
                pin.setMode("off");
            } else if (pin.getGpio() == clearPin && systemValue == 0) {
                pin.setMode("off");
            } else if (pin.getGpio() == clearPin && systemValue == 1) {
                pin.setMode("on");
            }
        }

        state.addResource(raspberry);
    }
}
