#!/usr/bin/env bash
# 두 번째 Endpoint(토스 계약) 결제 요청 (서버 기동 후 실행)
# 이 계약은 주문번호가 멱등성 키다. 같은 주문번호로 두 번 보내면 같은 paymentKey가 온다.
#   ./scripts/toss-pay.sh ORDER-1
ORDER_ID=${1:-ORDER-20260911-0001}
sed "s/ORDER-20260911-0001/${ORDER_ID}/" src/test/resources/soap/toss-pay-request.xml \
  | curl -s -X POST http://localhost:8080/services/toss-payment \
      -H "Content-Type: text/xml;charset=UTF-8" \
      -H 'SOAPAction: ""' \
      -d @-
echo
