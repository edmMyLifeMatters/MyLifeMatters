#!/bin/sh

curl -H "Content-type: application/json" \
     -H "Accept: application/json" \
     --data @test.json \
     -X POST \
     "http://localhost:8080/venti/rs/crud/test2"
