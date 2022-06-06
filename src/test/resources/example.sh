#!/bin/bash
val=$(echo $1*$2 | bc)
cat > $(pwd)/data.json <<- EOM
{
  "params": {
    "memoryRequest": $1,
    "cpuRequest": $2,
    "parallel": $3
  }
  "output": {
    "data": 1,
    "count": 100,
    "average": $val
    }
}
EOM
cat $(pwd)/data.json
