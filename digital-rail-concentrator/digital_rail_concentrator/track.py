from __future__ import annotations

import logging

from gpiozero import LED


class Signal:
    _label: str
    _stop: LED
    _clear: LED

    def __init__(self, label: str, stop: int, clear: int):
        self._label = label
        self._stop = LED(stop)
        self._clear = LED(clear)

        self._stop.on()
        self._clear.off()

        logging.info('Initialized signal %s with GPIOs stop=%d and clear=%d', label, stop, clear)

    def stop(self):
        self._stop.on()
        self._clear.off()

        logging.info('Set signal %s to stop', self._label)

    def clear(self):
        self._stop.off()
        self._clear.on()

        logging.info('Set signal %s to clear', self._label)


class Switch:
    _label: str
    _switch: LED

    def __init__(self, label: str, switch: int):
        self._label = label
        self._switch = LED(switch)
        self._switch.off()

        logging.info('Initialized switch %s with GPIO %d', label, switch)

    def base(self):
        self._switch.off()
        logging.info('Set switch %s to base position', self._label)

    def alternate(self):
        self._switch.on()
        logging.info('Set switch %s to alternate position', self._label)


class Track:
    _elements: dict[str, Signal | Switch]

    def __init__(self, elements: dict[str, Signal | Switch]):
        self._elements = elements

    def set(self, label: str, state: int):
        element = self._elements[label]
        if isinstance(element, Signal):
            if state == 0:
                element.stop()
            else:
                element.clear()
        elif isinstance(element, Switch):
            if state == 0:
                element.base()
            else:
                element.alternate()
        else:
            raise ValueError('Unknown element type %s' + str(type(element)))

    def reset(self):
        for element in self._elements.values():
            if isinstance(element, Signal):
                element.stop()
            elif isinstance(element, Switch):
                element.base()
            else:
                raise ValueError('Unknown element type %s' + str(type(element)))
