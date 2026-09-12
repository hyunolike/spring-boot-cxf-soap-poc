# WSDL diff — what one SEI method costs

Java First means the SEI *is* the contract. This file records what actually changed in
`http://localhost:8080/services/payment?wsdl` when a single method was added to
`PaymentService`:

```java
@WebMethod
@WebResult(name = "response")
CardInquiryResponse inquiry(@WebParam(name = "request") CardInquiryRequest request);
```

One Java method moved the WSDL in **five** places — 116 lines to 169.

| WSDL section | What appeared |
|---|---|
| `wsdl:types` / elements | `inquiry`, `inquiryResponse` global elements |
| `wsdl:types` / complexTypes | `inquiry`, `inquiryResponse` wrappers plus `CardInquiryRequest`, `CardInquiryResponse` |
| `wsdl:message` | `inquiry`, `inquiryResponse` |
| `wsdl:portType` | `<wsdl:operation name="inquiry">` with its input/output |
| `wsdl:binding` | `<wsdl:operation name="inquiry">` with `soapAction` and `use="literal"` bodies |

Worth noticing:

- **`xs:decimal` for `BigDecimal`** — `amount` crosses the wire as `xs:decimal`, not a string.
- **`minOccurs="0"` on every response field** — response DTOs have no `@XmlElement(required = true)`,
  so consumers must treat them as optional. Request fields that *are* marked required have no
  `minOccurs`, i.e. they default to 1.
- **Operation order is not source order** — CXF emitted `inquiry` between `approve` and `cancel`.
  Reflection does not preserve declaration order, so a WSDL diff between builds can be noisy
  even when nothing meaningful changed. Contract First does not have this problem.

## Reproducing

```bash
./gradlew bootRun
curl -s "http://localhost:8080/services/payment?wsdl" | xmllint --format - > after.wsdl
diff -u before.wsdl after.wsdl
```

## The diff

```diff
@@ -6,6 +6,8 @@
       <xs:element name="approveResponse" type="tns:approveResponse"/>
       <xs:element name="cancel" type="tns:cancel"/>
       <xs:element name="cancelResponse" type="tns:cancelResponse"/>
+      <xs:element name="inquiry" type="tns:inquiry"/>
+      <xs:element name="inquiryResponse" type="tns:inquiryResponse"/>
       <xs:complexType name="approve">
         <xs:sequence>
           <xs:element minOccurs="0" name="request" type="tns:CardApprovalRequest"/>
@@ -31,6 +33,34 @@
           <xs:element minOccurs="0" name="approvedAt" type="xs:string"/>
         </xs:sequence>
       </xs:complexType>
+      <xs:complexType name="inquiry">
+        <xs:sequence>
+          <xs:element minOccurs="0" name="request" type="tns:CardInquiryRequest"/>
+        </xs:sequence>
+      </xs:complexType>
+      <xs:complexType name="CardInquiryRequest">
+        <xs:sequence>
+          <xs:element name="merchantId" type="xs:string"/>
+          <xs:element name="approvalNo" type="xs:string"/>
+        </xs:sequence>
+      </xs:complexType>
+      <xs:complexType name="inquiryResponse">
+        <xs:sequence>
+          <xs:element minOccurs="0" name="response" type="tns:CardInquiryResponse"/>
+        </xs:sequence>
+      </xs:complexType>
+      <xs:complexType name="CardInquiryResponse">
+        <xs:sequence>
+          <xs:element minOccurs="0" name="resultCode" type="xs:string"/>
+          <xs:element minOccurs="0" name="resultMessage" type="xs:string"/>
+          <xs:element minOccurs="0" name="approvalNo" type="xs:string"/>
+          <xs:element minOccurs="0" name="status" type="xs:string"/>
+          <xs:element minOccurs="0" name="maskedCardNo" type="xs:string"/>
+          <xs:element minOccurs="0" name="amount" type="xs:decimal"/>
+          <xs:element minOccurs="0" name="approvedAt" type="xs:string"/>
+          <xs:element minOccurs="0" name="canceledAt" type="xs:string"/>
+        </xs:sequence>
+      </xs:complexType>
       <xs:complexType name="cancel">
         <xs:sequence>
           <xs:element minOccurs="0" name="request" type="tns:CardCancelRequest"/>
@@ -61,6 +91,14 @@
     <wsdl:part element="tns:approve" name="parameters">
     </wsdl:part>
   </wsdl:message>
+  <wsdl:message name="inquiry">
+    <wsdl:part element="tns:inquiry" name="parameters">
+    </wsdl:part>
+  </wsdl:message>
+  <wsdl:message name="inquiryResponse">
+    <wsdl:part element="tns:inquiryResponse" name="parameters">
+    </wsdl:part>
+  </wsdl:message>
   <wsdl:message name="approveResponse">
     <wsdl:part element="tns:approveResponse" name="parameters">
     </wsdl:part>
@@ -80,6 +118,12 @@
       <wsdl:output message="tns:approveResponse" name="approveResponse">
     </wsdl:output>
     </wsdl:operation>
+    <wsdl:operation name="inquiry">
+      <wsdl:input message="tns:inquiry" name="inquiry">
+    </wsdl:input>
+      <wsdl:output message="tns:inquiryResponse" name="inquiryResponse">
+    </wsdl:output>
+    </wsdl:operation>
     <wsdl:operation name="cancel">
       <wsdl:input message="tns:cancel" name="cancel">
     </wsdl:input>
@@ -98,6 +142,15 @@
         <soap:body use="literal"/>
       </wsdl:output>
     </wsdl:operation>
+    <wsdl:operation name="inquiry">
+      <soap:operation soapAction="" style="document"/>
+      <wsdl:input name="inquiry">
+        <soap:body use="literal"/>
+      </wsdl:input>
+      <wsdl:output name="inquiryResponse">
+        <soap:body use="literal"/>
+      </wsdl:output>
+    </wsdl:operation>
     <wsdl:operation name="cancel">
       <soap:operation soapAction="" style="document"/>
       <wsdl:input name="cancel">
```

## Follow-up: what the later roadmap items did to the same WSDL

Two more changes landed after the `inquiry` diff above. Both are instructive:

**Idempotency (`txId`) changed the contract.** Adding one required request field and one
response field rewrote the schema that every consumer validates against:

```diff
       <xs:complexType name="CardApprovalRequest">
         <xs:sequence>
           <xs:element name="merchantId" type="xs:string"/>
+          <xs:element name="txId" type="xs:string"/>
           <xs:element name="cardNo" type="xs:string"/>
           <xs:element name="amount" type="xs:decimal"/>
         </xs:sequence>
@@
           <xs:element minOccurs="0" name="approvedAt" type="xs:string"/>
+          <xs:element name="duplicated" type="xs:boolean"/>
```

`duplicated` is a primitive `boolean`, so JAXB emitted it **without** `minOccurs="0"` — unlike
the `String` fields next to it. A primitive cannot be absent, so the schema says it is mandatory.
Switching the field to `Boolean` would make it optional again. That is the kind of detail Java
First decides for you.

**WS-Security changed nothing.** With `WSS4JInInterceptor` wired in, an unauthenticated call gets

```xml
<soap:Fault>
  <faultcode xmlns:ns1="http://ws.apache.org/wss4j">ns1:SecurityError</faultcode>
  <faultstring>A security error was encountered when verifying the message</faultstring>
</soap:Fault>
```

…but the interceptor contributed **zero** lines to `?wsdl`: re-capturing it after wiring WSS4J in
produced only the `txId` / `duplicated` changes above. The requirement lives in an interceptor,
not in the published contract, so a consumer reading the WSDL has no way to learn that a
`wsse:UsernameToken` header is mandatory. Publishing a WS-SecurityPolicy assertion is what makes
it discoverable; this PoC does not, which is why `scripts/*.sh` carry the header by hand.
