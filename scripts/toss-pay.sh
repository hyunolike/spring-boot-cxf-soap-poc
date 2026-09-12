#!/usr/bin/env bash
# 두 번째 Endpoint(토스 계약) 결제 요청 (서버 기동 후 실행)
curl -s -X POST http://localhost:8080/services/toss-payment \
  -H "Content-Type: text/xml;charset=UTF-8" \
  -H 'SOAPAction: ""' \
  -d @src/test/resources/soap/toss-pay-request.xml
echo
