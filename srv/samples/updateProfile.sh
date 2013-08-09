#!/bin/sh

curl -H "Content-type: application/json" \
     -H "Accept: application/json" \
     -H "X-Venti-ClientID: sampleClientId" \
     -H "X-Venti-Hash: d0b356b5df98860cb5a8329b79fe148cfefca6b7f190b0a80a07844ada6f6c73" \
     --data @updateProfile.json \
     -X PUT \
     -k -3 \
     "https://samhsa.carewave.net/edm-mobile/rs/crud/profile/51e07aeee4b062b6bb402e3e"
