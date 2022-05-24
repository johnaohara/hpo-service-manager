#!/bin/bash
val=$(echo $1*$2 | bc)
echo "{\"data\": { \"data\": 1, \"count\": 100, \"average\": $val}}" > $(pwd)/data.json
cat $(pwd)/data.json
