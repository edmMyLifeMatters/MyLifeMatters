#!/bin/sh

curl -H "Content-type: application/json" \
     -H "Accept: application/json" \
     --data @test.json \
     -X PUT \
     "http://localhost:8080/venti/rs/crud/test2/51d481d130048aefbf66ce49"
