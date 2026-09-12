#!/usr/bin/env bash
# 카드 거래 조회 SOAP 요청 — 첫 번째 인자로 승인번호 전달
# 사용: ./scripts/inquiry.sh AP20260911123456
APPROVAL_NO=${1:?"승인번호를 인자로 전달하세요"}
sed "s/AP20260911123456/${APPROVAL_NO}/" src/test/resources/soap/inquiry-request.xml \
  | curl -s -X POST http://localhost:8080/services/payment \
      -H "Content-Type: text/xml;charset=UTF-8" \
      -H 'SOAPAction: ""' \
      -d @-
echo
