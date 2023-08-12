from __future__ import annotations

import json
import logging
from socket import socket, AF_INET, SOCK_STREAM
from threading import Thread

from digital_rail_concentrator.messaging import Message, ErrorMessage, MessageHandler


class ServerThread(Thread):
    def __init__(self, client: socket):
        Thread.__init__(self)
        self.client = client

    def run(self):
        peer_name = self.client.getpeername()
        handler = MessageHandler()
        while True:
            try:
                raw_message = self._read_line()
                if raw_message == '.':
                    self.client.close()
                    logging.info('Closed connection for client %s', peer_name)
                    return

                try:
                    message = Message.from_json(json.loads(raw_message))
                    response = handler.handle_message(message)
                    self.client.send((response.to_json() + '\n').encode('utf-8'))
                except Exception as e:
                    logging.exception('Failed to handle message: %s', e)
                    self.client.send((Message('ErrorMessage', ErrorMessage(str(e))).to_json() + '\n').encode('utf-8'))
            except ConnectionResetError or BrokenPipeError:
                logging.warning('Connection reset for client %s', peer_name)
                return

    def _read_line(self) -> str:
        buffer = b''
        while True:
            data = self.client.recv(1)
            if data == b'\n':
                return buffer.decode('utf-8')
            buffer += data


class SocketServer:
    _port: int

    def __init__(self, port: int):
        self._port = port

    def run(self):
        server = socket(AF_INET, SOCK_STREAM)
        server.bind(('', self._port))

        try:
            server.listen(5)

            logging.info('Socket server listening on port %d', self._port)

            while True:
                (client, address) = server.accept()
                logging.info('Accepted connection from %s', address)
                ServerThread(client).start()
        except KeyboardInterrupt:
            logging.info('Shutting down socket server gracefully')
            server.close()
            logging.info("Bye")
