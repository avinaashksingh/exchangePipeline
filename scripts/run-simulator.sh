#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
./mvnw -q exec:java -Dexec.mainClass=com.scetzhbook.exchangePipeline.simulator.OrderLoadSimulator "$@"
