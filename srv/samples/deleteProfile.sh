#!/bin/sh

curl -H "Content-type: application/json" \
     -H "Accept: application/json" \
     -H "X-Venti-ClientID: sampleClientId" \
     -H "X-Venti-Hash: e6a2a2ef015cf55351ca85121752c46f42756286dfe8915f4da2491f3993af05" \
     -X DELETE \
     "http://localhost:8080/samhsa/rs/crud/profile/51d35bf63004e98d7fef1396"
