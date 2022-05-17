#!/bin/bash

echo '{"data": { "count": 100, "average": 20}}' > $(pwd)/data.json
cat $(pwd)/data.json
