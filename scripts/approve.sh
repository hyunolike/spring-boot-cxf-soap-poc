#!/usr/bin/env bash
# 카드 승인 SOAP 요청 (서버 기동 후 실행)
# 첫 번째 인자로 거래고유번호를 넘기면 멱등성을 확인할 수 있다.
#   ./scripts/approve.sh TX-1   # 첫 승인
#   ./scripts/approve.sh TX-1   # 같은 승인번호 + duplicated=true
TX_ID=${1:-TX-20260911-0001}
sed "s/TX-20260911-0001/${TX_ID}/" src/test/resources/soap/approve-request.xml \
  | curl -s -X POST http://localhost:8080/services/payment \
      -H "Content-Type: text/xml;charset=UTF-8" \
      -H 'SOAPAction: ""' \
      -d @-
echo
