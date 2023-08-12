from __future__ import annotations

import json
from abc import ABC, abstractmethod
from dataclasses import dataclass
from typing import cast

from digital_rail_concentrator.track import Track, Signal, Switch


class JsonParseable(ABC):
    @staticmethod
    @abstractmethod
    def from_json(data: dict) -> JsonParseable:
        pass

    @abstractmethod
    def to_json(self) -> str:
        pass


@dataclass
class Message(JsonParseable):
    kind: str
    payload: JsonParseable

    @staticmethod
    def from_json(data: dict) -> Message:
        if data['kind'] == 'ErrorMessage':
            return Message(data['kind'], ErrorMessage.from_json(data['payload']))
        elif data['kind'] == 'CommandMessage':
            return Message(data['kind'], CommandMessage.from_json(data['payload']))
        elif data['kind'] == 'SetupMessage':
            return Message(data['kind'], SetupMessage.from_json(data['payload']))
        elif data['kind'] == 'AllocationMessage':
            return Message(data['kind'], AllocationMessage.from_json(data['payload']))

        raise ValueError('Unknown message kind: ' + data['kind'])

    def to_json(self) -> str:
        return json.dumps({
            'kind': self.kind,
            'payload': self.payload.to_json()
        })


@dataclass
class ErrorMessage(JsonParseable):
    error: str

    @staticmethod
    def from_json(data: dict) -> ErrorMessage:
        return ErrorMessage(data['error'])

    def to_json(self) -> str:
        return json.dumps({
            'error': self.error
        })


@dataclass
class CommandMessage(JsonParseable):
    command: str

    @staticmethod
    def from_json(data: dict) -> CommandMessage:
        return CommandMessage(data['command'])

    def to_json(self) -> str:
        return json.dumps({
            'command': self.command
        })


@dataclass
class SetupMessage(JsonParseable):
    elements: list[SetupElement]

    @staticmethod
    def from_json(data: dict) -> SetupMessage:
        return SetupMessage(list(map(lambda x: SetupElement.from_json(x), data['elements'])))

    def to_json(self) -> str:
        return json.dumps({
            'elements': list(map(lambda x: x.to_json(), self.elements))
        })


@dataclass
class SetupElement(JsonParseable):
    label: str
    kind: str
    spec: JsonParseable

    @staticmethod
    def from_json(data: dict) -> SetupElement:
        if data['kind'] == 'Signal':
            return SetupElement(data['label'], data['kind'], SetupSignal.from_json(data['spec']))
        elif data['kind'] == 'Switch':
            return SetupElement(data['label'], data['kind'], SetupSwitch.from_json(data['spec']))

        raise ValueError('Unknown element kind: ' + data['kind'])

    def to_json(self) -> str:
        return json.dumps({
            'label': self.label,
            'kind': self.kind,
            'spec': self.spec.to_json()
        })


@dataclass
class SetupSignal(JsonParseable):
    stop: int
    clear: int

    @staticmethod
    def from_json(data: dict) -> SetupSignal:
        return SetupSignal(data['stop'], data['clear'])

    def to_json(self) -> str:
        return json.dumps({
            'stop': self.stop,
            'clear': self.clear
        })


@dataclass
class SetupSwitch(JsonParseable):
    alternate: int

    @staticmethod
    def from_json(data: dict) -> SetupSwitch:
        return SetupSwitch(data['alternate'])

    def to_json(self) -> str:
        return json.dumps({
            'alternate': self.alternate
        })


@dataclass
class AllocationMessage(JsonParseable):
    receiver: str
    state: int

    @staticmethod
    def from_json(data: dict) -> AllocationMessage:
        return AllocationMessage(data['receiver'], data['state'])

    def to_json(self) -> str:
        return json.dumps({
            'receiver': self.receiver,
            'state': self.state
        })


class MessageHandler:
    _track: Track = None

    def handle_message(self, message: Message) -> Message:
        if message.kind == 'CommandMessage':
            msg = cast(CommandMessage, message.payload)
            if msg.command == 'reset':
                self._track.reset()
        elif message.kind == 'SetupMessage':
            msg = cast(SetupMessage, message.payload)
            elements: dict[str, Signal | Switch] = {}

            for element in msg.elements:
                if element.kind == 'Signal':
                    sig = cast(SetupSignal, element.spec)
                    elements[element.label] = Signal(element.label, sig.stop, sig.clear)
                elif element.kind == 'Switch':
                    switch = cast(SetupSwitch, element.spec)
                    elements[element.label] = Switch(element.label, switch.alternate)

            self._track = Track(elements)
        elif message.kind == 'AllocationMessage':
            msg = cast(AllocationMessage, message.payload)
            if self._track is None:
                raise ValueError('Setup track before allocating signals and switches')
            self._track.set(msg.receiver, msg.state)

        return message
