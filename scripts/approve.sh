#!/usr/bin/env bash
# 카드 승인 SOAP 요청 (서버 기동 후 실행)
curl -s -X POST http://localhost:8080/services/payment \
  -H "Content-Type: text/xml;charset=UTF-8" \
  -H 'SOAPAction: ""' \
  -d @src/test/resources/soap/approve-request.xml
echo
