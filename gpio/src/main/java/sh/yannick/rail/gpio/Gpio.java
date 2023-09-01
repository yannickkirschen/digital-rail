package sh.yannick.rail.gpio;

import java.util.List;

public interface Gpio {
    void raisePin(int gpio);

    void lowerPin(int gpio);

    void disablePin(int gpio);

    void setPin(int gpio, String mode);

    List<Integer> usedPins();
}
