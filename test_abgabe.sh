#!/bin/bash
cd "$(dirname "$0")"
java -Djava.library.path=lib/native/ -cp lib/ai.jar:lib/ai_libs.jar:out/artifacts/s0564478/s0564478.jar lenz.htw.ai4g.Simulator 1024 768 20 T