import logging

from digital_rail_concentrator.server import SocketServer


def main():
    SocketServer(6666).run()


if __name__ == '__main__':
    logging.basicConfig(level=logging.INFO, format='%(asctime)s %(levelname)s %(message)s')
    main()
