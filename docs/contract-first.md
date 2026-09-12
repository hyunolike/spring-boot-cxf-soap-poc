# Contract First vs Java First, side by side

Both versions of the card payment service run in this PoC, on the same business layer:

| | Java First | Contract First |
|---|---|---|
| Endpoint | `/services/payment` | `/services/card-payment-v1` |
| Namespace | `http://payment.poc.com/` | `http://contract.payment.poc.com/v1` |
| Source of truth | `PaymentService` (hand-written SEI) | `src/main/resources/wsdl/card-payment-v1.wsdl` (hand-written WSDL) |
| Generated | WSDL, at runtime, by CXF | `CardPaymentPortType` + JAXB types, at build time, by `wsdl2java` |
| Implementation | `PaymentServiceImpl` | `CardPaymentEndpoint implements CardPaymentPortType` |
| Business layer | `PaymentApplicationService` | the same `PaymentApplicationService` |

## How the codegen is wired

No third-party Gradle plugin. A `JavaExec` task runs the CXF tool off its own configuration, so the
codegen classpath never leaks into the application's:

```gradle
configurations { wsdl2java }

tasks.register('wsdl2java', JavaExec) {
    classpath = configurations.wsdl2java
    mainClass = 'org.apache.cxf.tools.wsdlto.WSDLToJava'
    args = ['-d', <generated dir>, '-p', '<namespace>=<package>',
            '-wsdlLocation', 'classpath:wsdl/card-payment-v1.wsdl', '-validate', <wsdl>]
}

sourceSets.main.java.srcDir generatedSourceDir
tasks.named('compileJava') { dependsOn 'wsdl2java' }
```

Generated sources land in `build/` and are **not** committed. Editing the WSDL and rebuilding is what
makes the Java change — that is the whole point of the direction of dependency.

## What the contract can say that the SEI could not

The hand-written schema carries facets that Java types have no way to express:

```xml
<xs:simpleType name="ApprovalNo">
  <xs:restriction base="xs:string"><xs:pattern value="AP[0-9]{14}"/></xs:restriction>
</xs:simpleType>

<xs:simpleType name="Amount">
  <xs:restriction base="xs:decimal">
    <xs:minExclusive value="0"/><xs:totalDigits value="15"/><xs:fractionDigits value="0"/>
  </xs:restriction>
</xs:simpleType>
```

With `schema-validation-enabled` on the endpoint, those facets are enforced before any business code
runs. The same invalid amount gets two different answers from the two endpoints:

```bash
# Contract First — rejected at the XML layer
$ ./scripts/contract-approve.sh TX-1 -100
<soap:Fault><faultcode>soap:Client</faultcode><faultstring>Unmarshalling Error:
cvc-minExclusive-valid: Value '-100' is not facet-valid with respect to minExclusive '0.0'
for type 'Amount'.</faultstring></soap:Fault>

# Java First — accepted, then refused by PaymentApplicationService.validate()
<response><resultCode>1001</resultCode><resultMessage>금액은 0보다 커야 합니다</resultMessage></response>
```

`xs:enumeration` also becomes a generated Java `enum` (`PaymentStatus.APPROVED`), so a typo in a
status string fails at compile time instead of at the consumer.

## What it costs

- **`xs:dateTime` maps to `XMLGregorianCalendar`.** `ContractCardMapper` converts every
  `LocalDateTime` by hand. Escaping that means adding a JAXB binding file (`.xjb`) with an
  `XmlAdapter` — more build machinery, which Java First never needed.
- **The WSDL is yours to keep correct.** `elementFormDefault="qualified"` means every child element
  in a request carries the namespace (see `src/test/resources/soap/contract-approve-request.xml`);
  get it wrong by hand and the server rejects the message.
- **Two layers of failure to reason about.** A contract violation is a `soap:Fault`; a business
  violation is a result code. Consumers now have to handle both shapes.
- **`BARE` parameter style.** `wsdl2java` generated `@SOAPBinding(parameterStyle = BARE)` because the
  messages carry a single element part. The Java First SEI uses the default `WRAPPED`. The wire
  format differs even though the Java signatures look alike.

## Which one for a 대외계

Contract First, when the contract is agreed before the code exists — which is the normal case for an
external-facing payment interface. The published WSDL then stops being a build artifact and starts
being a document both sides signed, and `schema-validation-enabled` turns that document into a
runtime guard for free.

Java First stays useful for internal services where one team owns both ends and the contract can
follow the code.
