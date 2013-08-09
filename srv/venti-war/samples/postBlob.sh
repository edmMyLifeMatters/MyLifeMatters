#!/bin/sh

curl -H "Content-type: image/png" \
     -H "Accept: application/json" \
     -k -x localhost:8888 \
     --data-binary @USA.png \
     -X POST \
     "http://localhost:8080/venti/rs/crud/blob"
