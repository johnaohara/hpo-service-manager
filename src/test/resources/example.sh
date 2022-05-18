#!/bin/bash
echo '{"data": { "data": 1, "count": 100, "average": 20}}' > $(pwd)/data.json
cat $(pwd)/data.json
