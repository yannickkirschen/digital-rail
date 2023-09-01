package sh.yannick.rail.gpio;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DummyGpio implements Gpio {
    private final Map<Integer, Boolean> pins = new HashMap<>();

    @Override
    public void raisePin(int gpio) {
        if (pins.containsKey(gpio)) {
            pins.put(gpio, true);
        }
    }

    @Override
    public void lowerPin(int gpio) {
        if (pins.containsKey(gpio)) {
            pins.put(gpio, false);
        }
    }

    @Override
    public void disablePin(int gpio) {
        pins.remove(gpio);
    }

    @Override
    public void setPin(int gpio, String mode) {
        pins.put(gpio, "on".equals(mode));
    }

    @Override
    public List<Integer> usedPins() {
        return pins.keySet().stream().toList();
    }
}
