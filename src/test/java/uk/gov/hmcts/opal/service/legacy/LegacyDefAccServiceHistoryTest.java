package uk.gov.hmcts.opal.service.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryFilter;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryResponse;
import uk.gov.hmcts.opal.dto.history.DefendantTransactionDetails;
import uk.gov.hmcts.opal.dto.history.EnforcementDetails;
import uk.gov.hmcts.opal.dto.history.HistoryItemType;
import uk.gov.hmcts.opal.dto.history.NoteDetails;
import uk.gov.hmcts.opal.dto.history.PaymentTermsDetails;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountHistoryLegacyRequest;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountHistoryLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountHistoryLegacyResponse.LegacyDefendantAccountHistoryDetails;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountHistoryLegacyResponse.LegacyDefendantAccountHistoryItem;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountHistoryLegacyResponse.LegacyHistoryTypeReference;
import uk.gov.hmcts.opal.dto.legacy.LegacyInstalmentPeriod;
import uk.gov.hmcts.opal.dto.legacy.LegacyPaymentTermsType;
import uk.gov.hmcts.opal.dto.legacy.LegacyPostedDetails;
import uk.gov.hmcts.opal.dto.legacy.common.CourtReference;

class LegacyDefAccServiceHistoryTest extends AbstractLegacyDefAccServiceTest {

    @Test
    void createGetDefendantAccountHistoryRequest_mapsFilterToLegacyRequest() {
        // Arrange
        DefendantAccountHistoryFilter filter = DefendantAccountHistoryFilter.builder()
            .dateFrom(LocalDate.of(2026, 5, 11))
            .dateTo(LocalDate.of(2026, 5, 12))
            .itemTypes(List.of(HistoryItemType.ENFORCEMENT, HistoryItemType.PAYMENT_TERMS, HistoryItemType.NOTE))
            .build();

        // Act
        GetDefendantAccountHistoryLegacyRequest request =
            LegacyDefendantAccountService.createGetDefendantAccountHistoryRequest(99000000000001L, filter);

        // Assert
        assertEquals("99000000000001", request.getDefendantAccountId());
        assertEquals(LocalDate.of(2026, 5, 11), request.getFromDate());
        assertEquals(LocalDate.of(2026, 5, 12), request.getToDate());
        assertEquals(List.of("Enforcement", "Payment Terms", "Note"), request.getItemTypes());
    }

    @SuppressWarnings("unchecked")
    @Test
    void getHistory_success_mapsMixedLegacyHistoryItems() {
        // Arrange
        GetDefendantAccountHistoryLegacyResponse responseBody = GetDefendantAccountHistoryLegacyResponse.builder()
            .version(9L)
            .historyItems(List.of(
                amendmentItem(),
                paymentTermsItem(),
                enforcementItem(),
                financialItem(),
                noteItem()
            ))
            .build();

        when(restClient.responseSpec.body(
            Mockito.<ParameterizedTypeReference<GetDefendantAccountHistoryLegacyResponse>>any()
        )).thenReturn(responseBody);
        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>(responseBody.toXml(), HttpStatus.OK));

        DefendantAccountHistoryFilter filter = DefendantAccountHistoryFilter.builder()
            .dateFrom(LocalDate.of(2026, 5, 11))
            .dateTo(LocalDate.of(2026, 5, 12))
            .itemTypes(List.of(HistoryItemType.ENFORCEMENT, HistoryItemType.NOTE))
            .build();

        // Act
        DefendantAccountHistoryResponse out = legacyDefendantAccountService.getHistory(99000000000001L, filter);

        // Assert
        assertNotNull(out);
        assertEquals(BigInteger.valueOf(9L), out.getVersion());
        assertEquals(5, out.getHistoryItems().size());
        assertEquals(HistoryItemType.FINANCIAL, out.getHistoryItems().get(0).getType());
        assertEquals(HistoryItemType.PAYMENT_TERMS, out.getHistoryItems().get(1).getType());
        assertEquals("Reason text",
            ((EnforcementDetails) out.getHistoryItems().get(3).getDetails()).getReason());
        assertEquals("Account note text",
            ((NoteDetails) out.getHistoryItems().get(2).getDetails()).getNoteText());
        assertEquals("Extension reason",
            ((PaymentTermsDetails) out.getHistoryItems().get(1).getDetails()).getReasonForExtension());
        assertEquals("PAY123",
            ((DefendantTransactionDetails) out.getHistoryItems().get(0).getDetails()).getPaymentReference());
        assertEquals(new BigDecimal("-25.50"), out.getHistoryItems().get(0).getAmount());
        assertEquals(LocalDateTime.of(2026, 5, 12, 10, 15), out.getHistoryItems().get(0).getEventDateTime());
        assertEquals(HistoryItemType.AMENDMENT, out.getHistoryItems().get(4).getType());

        ArgumentCaptor<GetDefendantAccountHistoryLegacyRequest> requestCaptor =
            ArgumentCaptor.forClass(GetDefendantAccountHistoryLegacyRequest.class);
        verify(gatewayService).postToGateway(
            eq(LegacyDefendantAccountService.GET_DEFENDANT_ACCOUNT_HISTORY),
            eq(GetDefendantAccountHistoryLegacyResponse.class),
            requestCaptor.capture(),
            eq(null)
        );
        assertEquals("99000000000001", requestCaptor.getValue().getDefendantAccountId());
        assertEquals(List.of("Enforcement", "Note"), requestCaptor.getValue().getItemTypes());
    }

    @SuppressWarnings("unchecked")
    @Test
    void getHistory_success_withNullEntity_returnsDefaultEmptyResponse() {
        // Arrange
        when(restClient.responseSpec.body(
            Mockito.<ParameterizedTypeReference<GetDefendantAccountHistoryLegacyResponse>>any()
        )).thenReturn(null);
        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>("<response/>", HttpStatus.OK));

        // Act
        DefendantAccountHistoryResponse out =
            legacyDefendantAccountService.getHistory(1L, DefendantAccountHistoryFilter.builder().build());

        // Assert
        assertNotNull(out);
        assertEquals(BigInteger.ONE, out.getVersion());
        assertNotNull(out.getHistoryItems());
        assertEquals(0, out.getHistoryItems().size());
    }

    @Test
    void getHistory_legacyXmlResponseUnmarshalsCamelCaseHistoryFields() throws JAXBException {
        String legacyXml = """
            <response>
              <version>7</version>
              <historyItems>
                <historyItems_element>
                  <postedDetails>
                    <posted_date>2026-05-10T11:00:00</posted_date>
                    <posted_by>legacy-amend</posted_by>
                    <posted_by_name>Legacy Amend User</posted_by_name>
                  </postedDetails>
                  <type>Amendment</type>
                  <details>
                    <attributeName>Account status</attributeName>
                    <oldValue>Old</oldValue>
                    <newValue>New</newValue>
                  </details>
                </historyItems_element>
                <historyItems_element>
                  <postedDetails>
                    <posted_date>2026-05-12T12:00:00</posted_date>
                    <posted_by>legacy-fin</posted_by>
                    <posted_by_name>Legacy Financial User</posted_by_name>
                  </postedDetails>
                  <type>Financial</type>
                  <details>
                    <transactionType>
                      <transactionType>PAYMNT</transactionType>
                      <transactionTypeDisplayName>Payment</transactionTypeDisplayName>
                    </transactionType>
                    <paymentMethod>
                      <paymentMethod>NC</paymentMethod>
                      <paymentMethodDisplayName>National cheque</paymentMethodDisplayName>
                    </paymentMethod>
                    <paymentReference>PAY123</paymentReference>
                    <additionalInformation>Legacy payment</additionalInformation>
                    <status>
                      <defendantTransactionStatus>P</defendantTransactionStatus>
                      <defendantTransactionStatusDisplayName>Partially-reversed</defendantTransactionStatusDisplayName>
                    </status>
                    <statusDate>2026-05-12T12:05:00</statusDate>
                    <associatedRecordType>defendant_accounts</associatedRecordType>
                    <associatedRecordId>99000000000001</associatedRecordId>
                    <accountNumber>ACC-99000000000001</accountNumber>
                    <sendingCourt>Legacy Court</sendingCourt>
                  </details>
                  <amount>-25.50</amount>
                </historyItems_element>
                <historyItems_element>
                  <postedDetails>
                    <posted_date>2026-05-12T13:00:00</posted_date>
                    <posted_by>legacy-note</posted_by>
                    <posted_by_name>Legacy Note User</posted_by_name>
                  </postedDetails>
                  <type>Note</type>
                  <details>
                    <noteText>Legacy account note</noteText>
                  </details>
                </historyItems_element>
                <historyItems_element>
                  <postedDetails>
                    <posted_date>2026-05-12T09:30:00</posted_date>
                    <posted_by>legacy-pt</posted_by>
                    <posted_by_name>Legacy Terms User</posted_by_name>
                  </postedDetails>
                  <type>Payment terms</type>
                  <details>
                    <daysInDefault>7</daysInDefault>
                    <date_days_in_default_imposed>2026-05-12</date_days_in_default_imposed>
                    <reason_for_extension>Legacy extension</reason_for_extension>
                    <payment_terms_type>
                      <payment_terms_type_code>I</payment_terms_type_code>
                    </payment_terms_type>
                    <effective_date>2026-05-20</effective_date>
                    <instalment_period>
                      <installment_period_code>M</installment_period_code>
                    </instalment_period>
                    <lump_sum_amount>100.00</lump_sum_amount>
                    <instalment_amount>25.00</instalment_amount>
                  </details>
                </historyItems_element>
              </historyItems>
            </response>
            """;

        GetDefendantAccountHistoryLegacyResponse response =
            (GetDefendantAccountHistoryLegacyResponse) JAXBContext
                .newInstance(GetDefendantAccountHistoryLegacyResponse.class)
                .createUnmarshaller()
                .unmarshal(new StringReader(legacyXml));

        assertEquals(7L, response.getVersion());
        assertEquals(4, response.getHistoryItems().size());

        LegacyDefendantAccountHistoryItem amendment = response.getHistoryItems().get(0);
        assertEquals(LocalDateTime.of(2026, 5, 10, 11, 0), amendment.getPostedDetails().getPostedDate());
        assertEquals("Account status", amendment.getDetails().getAttributeName());
        assertEquals("Old", amendment.getDetails().getOldValue());
        assertEquals("New", amendment.getDetails().getNewValue());

        LegacyDefendantAccountHistoryItem financial = response.getHistoryItems().get(1);
        assertEquals(new BigDecimal("-25.50"), financial.getAmount());
        assertEquals("PAYMNT", financial.getDetails().getTransactionType().getTransactionType());
        assertEquals("NC", financial.getDetails().getPaymentMethod().getPaymentMethod());
        assertEquals("PAY123", financial.getDetails().getPaymentReference());
        assertEquals("P", financial.getDetails().getStatus().getDefendantTransactionStatus());
        assertEquals(LocalDateTime.of(2026, 5, 12, 12, 5), financial.getDetails().getStatusDate());
        assertEquals("99000000000001", financial.getDetails().getAssociatedRecordId());
        assertEquals("ACC-99000000000001", financial.getDetails().getAccountNumber());
        assertEquals("Legacy Court", financial.getDetails().getSendingCourt());

        LegacyDefendantAccountHistoryItem note = response.getHistoryItems().get(2);
        assertEquals("Legacy account note", note.getDetails().getNoteText());

        LegacyDefendantAccountHistoryItem paymentTerms = response.getHistoryItems().get(3);
        assertEquals(7, paymentTerms.getDetails().getDaysInDefault());
        assertEquals(LocalDate.of(2026, 5, 12), paymentTerms.getDetails().getDateDaysInDefaultImposed());
        assertEquals("Legacy extension", paymentTerms.getDetails().getReasonForExtension());
        assertEquals(LegacyPaymentTermsType.PaymentTermsTypeCode.I,
                     paymentTerms.getDetails().getPaymentTermsType().getPaymentTermsTypeCode());
        assertEquals(LocalDate.of(2026, 5, 20), paymentTerms.getDetails().getEffectiveDate());
        assertEquals(LegacyInstalmentPeriod.InstalmentPeriodCode.M,
                     paymentTerms.getDetails().getInstalmentPeriod().getInstalmentPeriodCode());
        assertEquals(new BigDecimal("100.00"), paymentTerms.getDetails().getLumpSumAmount());
        assertEquals(new BigDecimal("25.00"), paymentTerms.getDetails().getInstalmentAmount());
    }

    private LegacyDefendantAccountHistoryItem amendmentItem() {
        return LegacyDefendantAccountHistoryItem.builder()
            .postedDetails(postedDetails(2026, 5, 10, 9, 0, "amend-user", "Amend User"))
            .type("Amendment")
            .details(LegacyDefendantAccountHistoryDetails.builder()
                .attributeName("Account status")
                .oldValue("Old")
                .newValue("New")
                .build())
            .build();
    }

    private LegacyDefendantAccountHistoryItem enforcementItem() {
        return LegacyDefendantAccountHistoryItem.builder()
            .postedDetails(postedDetails(2026, 5, 11, 9, 30, "enf-user", "Enforcement User"))
            .type("Enforcement")
            .details(LegacyDefendantAccountHistoryDetails.builder()
                .enforcementAction("HST01")
                .daysInDefault(14)
                .warrantNumber("WR123")
                .hearingDate(LocalDate.of(2026, 6, 1))
                .hearingCourt(CourtReference.builder().courtId(44L).courtName("North Court").build())
                .caseNumber("CASE-1")
                .reason("Reason text")
                .earliestDateOfRelease(LocalDate.of(2026, 7, 1))
                .build())
            .build();
    }

    private LegacyDefendantAccountHistoryItem noteItem() {
        return LegacyDefendantAccountHistoryItem.builder()
            .postedDetails(postedDetails(2026, 5, 11, 12, 0, "note-user", "Note User"))
            .type("Note")
            .details(LegacyDefendantAccountHistoryDetails.builder()
                .noteText("Account note text")
                .build())
            .build();
    }

    private LegacyDefendantAccountHistoryItem paymentTermsItem() {
        return LegacyDefendantAccountHistoryItem.builder()
            .postedDetails(postedDetails(2026, 5, 12, 8, 45, "pt-user", "Payment Terms User"))
            .type("Payment terms")
            .details(LegacyDefendantAccountHistoryDetails.builder()
                .daysInDefault(7)
                .dateDaysInDefaultImposed(LocalDate.of(2026, 5, 12))
                .reasonForExtension("Extension reason")
                .paymentTermsType(new LegacyPaymentTermsType(LegacyPaymentTermsType.PaymentTermsTypeCode.I))
                .effectiveDate(LocalDate.of(2026, 5, 20))
                .instalmentPeriod(new LegacyInstalmentPeriod(LegacyInstalmentPeriod.InstalmentPeriodCode.M))
                .lumpSumAmount(new BigDecimal("100.00"))
                .instalmentAmount(new BigDecimal("25.00"))
                .build())
            .build();
    }

    private LegacyDefendantAccountHistoryItem financialItem() {
        return LegacyDefendantAccountHistoryItem.builder()
            .postedDetails(postedDetails(2026, 5, 12, 10, 15, "fin-user", "Financial User"))
            .type("Financial")
            .amount(new BigDecimal("-25.50"))
            .details(LegacyDefendantAccountHistoryDetails.builder()
                .transactionType(LegacyHistoryTypeReference.builder()
                    .transactionType("PAYMNT")
                    .transactionTypeDisplayName("Payment")
                    .build())
                .paymentMethod(LegacyHistoryTypeReference.builder()
                    .paymentMethod("NC")
                    .paymentMethodDisplayName("National cheque")
                    .build())
                .paymentReference("PAY123")
                .additionalInformation("Extra info")
                .writeOff(LegacyHistoryTypeReference.builder()
                    .writeOffType("TRNOUT")
                    .writeOffTypeDisplayName("Transfer out")
                    .build())
                .status(LegacyHistoryTypeReference.builder()
                    .defendantTransactionStatus("P")
                    .defendantTransactionStatusDisplayName("Partially-reversed")
                    .build())
                .statusDate(LocalDateTime.of(2026, 5, 12, 10, 20))
                .associatedRecordType("defendant_accounts")
                .associatedRecordId("262200")
                .accountNumber("262200A")
                .sendingCourt("North Court")
                .impositionDate(LocalDate.of(2026, 5, 12))
                .impositionCode("HST01")
                .amountImposed(new BigDecimal("-125.00"))
                .build())
            .build();
    }

    private LegacyPostedDetails postedDetails(int year, int month, int day, int hour, int minute, String postedBy,
                                              String postedByName) {
        return new LegacyPostedDetails(LocalDateTime.of(year, month, day, hour, minute), postedBy, postedByName);
    }
}
