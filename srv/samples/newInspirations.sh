#!/bin/sh

curl -H "Content-type: application/json" \
     -H "Accept: application/json" \
     -H "X-Venti-ClientID: sampleClientId" \
     -H "X-Venti-Hash: c1b036a592421f3f43c1e0e16e4d2e7f200e71714f178ec3b4c8e1efa7b782f6" \
     --data @newInspirations.json \
     -X POST \
     "http://localhost:8080/edm-mobile/rs/crud/inspiration"
