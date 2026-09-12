#!/usr/bin/env bash
# Contract First Endpoint 승인 요청 (서버 기동 후 실행)
# 같은 업무를 Java First Endpoint와 비교해 보려면 ./scripts/approve.sh 와 나란히 실행한다.
#   ./scripts/contract-approve.sh TX-CF-1          # 정상 승인
#   ./scripts/contract-approve.sh TX-CF-2 -100     # 스키마 위반 -> Fault (비즈니스 코드에 닿지 않는다)
TX_ID=${1:-TX-CF-20260911-0001}
AMOUNT=${2:-11000}
sed -e "s/TX-CF-20260911-0001/${TX_ID}/" -e "s#<v1:amount>11000#<v1:amount>${AMOUNT}#" \
    src/test/resources/soap/contract-approve-request.xml \
  | curl -s -X POST http://localhost:8080/services/card-payment-v1 \
      -H "Content-Type: text/xml;charset=UTF-8" \
      -H 'SOAPAction: "http://contract.payment.poc.com/v1/approveCard"' \
      -d @-
echo
