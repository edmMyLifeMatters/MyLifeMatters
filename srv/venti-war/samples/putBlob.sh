#!/bin/sh

curl -H "Content-type: image/png" \
     -H "Accept: application/json" \
     -k -x localhost:8888 \
     --data-binary @USA.png \
     -X PUT \
     "http://localhost:8080/venti/rs/crud/blob/51d49fb730048aefbf66ce4e"
