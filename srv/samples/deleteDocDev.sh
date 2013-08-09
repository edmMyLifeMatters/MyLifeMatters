#!/bin/sh

_BASE_URL=http://localhost:8080/edm-mobile
_COLLECTION_NAME=$1
_DOCUMENT_ID=$2

echo "Deleting $2 from $1."

curl -H "Content-type: application/json" \
     -H "Accept: application/json" \
     -H "X-Venti-ClientID: sampleClientId" \
     -H "X-Venti-Hash: d0b356b5df98860cb5a8329b79fe148cfefca6b7f190b0a80a07844ada6f6c73" \
     -X DELETE \
     -k -3 \
     "$_BASE_URL/rs/crud/$_COLLECTION_NAME/$_DOCUMENT_ID"
