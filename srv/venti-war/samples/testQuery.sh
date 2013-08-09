#!/bin/sh

curl -H "Content-type: application/json" \
     -H "Accept: application/json" \
     "http://localhost:8080/venti/rs/crud/test?query=%7B+%22message%22%3A+%7B+%22%24regex%22%3A+%22ello%22%2C+%22%24options%22%3A+%22i%22+%7D+%7D"
