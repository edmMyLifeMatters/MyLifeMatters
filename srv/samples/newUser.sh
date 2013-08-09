#!/bin/sh

_BASE_URL="http://localhost:8080/edm-mobile"

curl -H "Content-type: application/json" \
     -H "Accept: application/json" \
     -H "X-Venti-ClientID: sampleClientId" \
     -H "X-Venti-Hash: c1b036a592421f3f43c1e0e16e4d2e7f200e71714f178ec3b4c8e1efa7b782f6" \
     --data-ascii @credentials.json \
     -X POST \
     -k -x http://localhost:8888 \
     -ssl3 -k \
     "$_BASE_URL/rs/api/user/create"