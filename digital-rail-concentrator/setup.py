#!/usr/bin/env python3

"""Setup script for digital-rail-concentrator."""

from configparser import RawConfigParser

import setuptools

if __name__ == '__main__':
    config = RawConfigParser()
    config.read('setup.cfg')
    version = config.get('metadata', 'version')

    with open('digital_rail_concentrator/__version__.py', 'r', encoding='utf-8') as file:
        newText = file.read().replace('development', version)

    with open('digital_rail_concentrator/__version__.py', 'w', encoding='utf-8') as file:
        file.write(newText)

    setuptools.setup(
        entry_points={
            'console_scripts': [
                'rail-concentrator=digital_rail_concentrator.__main__:main',
            ]
        }
    )
