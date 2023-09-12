#!/usr/bin/env bash

if [[ "$EUID" != 0 ]]; then
    echo "Pi4J needs to be run as root. Try using sudo ./run.sh"
    exit 1
fi

java -classpath "$(dirname "$(realpath "$0")")/*" sh.yannick.rail.concentrator.Main "$@"
