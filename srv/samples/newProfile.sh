#!/bin/sh

curl -H "Content-type: application/json" \
     -H "Accept: application/json" \
     -H "X-Venti-ClientID: sampleClientId" \
     -H "X-Venti-Hash: c1b036a592421f3f43c1e0e16e4d2e7f200e71714f178ec3b4c8e1efa7b782f6" \
     --data @newProfile.json \
     -X POST \
     -ssl3 -k \
     "https://samhsa.carewave.net/edm-mobile/rs/crud/profile"
