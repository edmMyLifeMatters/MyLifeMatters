#!/bin/sh

curl -H "Content-type: application/json" \
     -H "Accept: application/json" \
     -X DELETE \
     "http://localhost:8080/venti/rs/crud/blob/51d4a45430048aefbf66ce4f"
