#!/bin/bash
val=$(echo $1*$2 | bc)
cat > $(pwd)/data.json <<- EOM
{ "data": {
    "params": {
      "memoryRequest": $1,
      "cpuRequest": $2,
      "parallel": $3
    },
    "results": {
      "data": 1,
      "count": 100,
      "average": $val
    }
  }
}
EOM
cat $(pwd)/data.json
