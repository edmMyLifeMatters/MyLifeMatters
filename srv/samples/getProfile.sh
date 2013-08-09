#!/bin/sh

curl -H "Content-type: application/json" \
     -H "Accept: application/json" \
     -H "X-Venti-ClientID: sampleClientId" \
     -H "X-Venti-Hash: d0b356b5df98860cb5a8329b79fe148cfefca6b7f190b0a80a07844ada6f6c73" \
     -k -x localhost:8888 \
     "http://localhost:8080/samhsa/rs/crud/profile/51d35c3e3004e98d7fef1397"
