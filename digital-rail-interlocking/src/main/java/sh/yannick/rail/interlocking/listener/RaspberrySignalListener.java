package sh.yannick.rail.interlocking.listener;

import lombok.extern.slf4j.Slf4j;
import sh.yannick.rail.api.resource.Raspberry;
import sh.yannick.rail.api.resource.Signal;
import sh.yannick.state.Listener;
import sh.yannick.state.ResourceListener;
import sh.yannick.state.State;

@Slf4j
@Listener(apiVersion = "rail.yannick.sh/v1alpha1", kind = "Signal")
public class RaspberrySignalListener implements ResourceListener<Signal.Spec, Signal.Status, Signal> {
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
        Raspberry raspberry = state.getResource("embedded.yannick.sh/v1alpha1", "Raspberry", "rpi", Raspberry.class).orElseThrow();

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
