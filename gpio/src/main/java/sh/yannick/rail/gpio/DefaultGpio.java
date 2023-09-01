package sh.yannick.rail.gpio;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalOutputConfigBuilder;
import com.pi4j.io.gpio.digital.DigitalState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultGpio implements Gpio {
    private final Context pi4j = Pi4J.newAutoContext();
    private final Map<Integer, DigitalOutput> pins = new HashMap<>();

    @Override
    public void raisePin(int gpio) {
        if (pins.containsKey(gpio)) {
            pins.get(gpio).high();
        }
    }

    @Override
    public void lowerPin(int gpio) {
        if (pins.containsKey(gpio)) {
            pins.get(gpio).low();
        }
    }

    @Override
    public void disablePin(int gpio) {
        if (pins.containsKey(gpio)) {
            pins.get(gpio).low();
            pins.remove(gpio);
        }
    }

    @Override
    public void setPin(int gpio, String mode) {
        if (pins.containsKey(gpio)) {
            pins.put(gpio, setPin(pins.get(gpio), mode));
            return;
        }

        DigitalOutputConfigBuilder ledConfig = DigitalOutput.newConfigBuilder(pi4j)
            .id("pin-" + gpio)
            .name("Pin " + gpio)
            .address(gpio)
            .shutdown(DigitalState.LOW)
            .initial(DigitalState.LOW)
            .provider("pigpio-digital-output");

        pins.put(gpio, setPin(pi4j.create(ledConfig), mode));
    }

    @Override
    public List<Integer> usedPins() {
        return pins.keySet().stream().toList();
    }

    private DigitalOutput setPin(DigitalOutput pin, String mode) {
        return switch (mode) {
            case "on" -> pin.high();
            case "off" -> pin.low();
            default -> throw new IllegalArgumentException("Unknown mode: " + mode);
        };
    }
}
