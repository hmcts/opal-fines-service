package uk.gov.hmcts.opal.mapper.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.opal.dto.legacy.GetMajorCreditorAccountHistoryLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.GetMajorCreditorAccountHistoryLegacyResponse.LegacyCreditorTransactionStatusReference;
import uk.gov.hmcts.opal.dto.legacy.GetMajorCreditorAccountHistoryLegacyResponse.LegacyCreditorTransactionTypeReference;
import uk.gov.hmcts.opal.dto.legacy.GetMajorCreditorAccountHistoryLegacyResponse.LegacyMajorCreditorHistoryDetails;
import uk.gov.hmcts.opal.dto.legacy.GetMajorCreditorAccountHistoryLegacyResponse.LegacyMajorCreditorHistoryItem;
import uk.gov.hmcts.opal.dto.legacy.LegacyPostedDetails;
import uk.gov.hmcts.opal.dto.response.GetMajorCreditorHistoryResponse;
import uk.gov.hmcts.opal.generated.model.CreditorTransactionDetailsHistory;
import uk.gov.hmcts.opal.generated.model.CreditorTransactionStatusReferenceCommon;
import uk.gov.hmcts.opal.generated.model.CreditorTransactionTypeReferenceCommon;
import uk.gov.hmcts.opal.generated.model.MajorCreditorHistoryItemHistory;
import uk.gov.hmcts.opal.mapper.AbstractMapperTest;

class GetMajorCreditorAccountHistoryResponseLegacyMapperTest extends AbstractMapperTest {

    @Autowired
    private GetMajorCreditorAccountHistoryResponseLegacyMapper mapper;

    @Test
    void toOpal_mapsLegacyHistoryResponse() {
        GetMajorCreditorAccountHistoryLegacyResponse legacy =
            GetMajorCreditorAccountHistoryLegacyResponse.builder()
                .version(7L)
                .historyItems(List.of(LegacyMajorCreditorHistoryItem.builder()
                    .postedDetails(new LegacyPostedDetails(
                        LocalDateTime.of(2026, 1, 31, 10, 30),
                        "MJUSR3",
                        "Major User Three"
                    ))
                    .type("Financial")
                    .amount(new BigDecimal("-31.00"))
                    .details(LegacyMajorCreditorHistoryDetails.builder()
                        .transactionType(LegacyCreditorTransactionTypeReference.builder()
                            .transactionType("MADJ")
                            .transactionTypeDisplayName("Manual Adjustment")
                            .build())
                        .paymentReference("MJF003")
                        .status(LegacyCreditorTransactionStatusReference.builder()
                            .creditorTransactionStatus("R")
                            .creditorTransactionStatusDisplayName("Reversed")
                            .build())
                        .statusDate(LocalDateTime.of(2026, 1, 31, 10, 30))
                        .associatedRecordType("creditor_accounts")
                        .associatedRecordId("99264300000001")
                        .accountNumber("87654321")
                        .defendantAccountNumber("12345678")
                        .defendantAccountId(999L)
                        .build())
                    .build()))
                .build();

        GetMajorCreditorHistoryResponse result = mapper.toOpal(legacy);

        assertEquals(BigInteger.valueOf(7), result.getVersion());
        assertNotNull(result.getPayload());
        assertEquals(1, result.getPayload().getHistoryItems().size());

        MajorCreditorHistoryItemHistory item = result.getPayload().getHistoryItems().getFirst();
        assertEquals(MajorCreditorHistoryItemHistory.TypeEnum.FINANCIAL, item.getType());
        assertEquals(new BigDecimal("-31.00"), item.getAmount());
        assertEquals("2026-01-31", item.getPostedDetails().getPostedDate().toString());
        assertEquals("MJUSR3", item.getPostedDetails().getPostedBy());
        assertEquals("Major User Three", item.getPostedDetails().getPostedByName());

        CreditorTransactionDetailsHistory details = item.getDetails();
        assertEquals(
            CreditorTransactionTypeReferenceCommon.TransactionTypeEnum.MADJ,
            details.getTransactionType().getTransactionType()
        );
        assertEquals("Manual Adjustment", details.getTransactionType().getTransactionTypeDisplayName());
        assertEquals("MJF003", details.getPaymentReference());
        assertEquals(
            CreditorTransactionStatusReferenceCommon.CreditorTransactionStatusEnum.R,
            details.getStatus().getCreditorTransactionStatus()
        );
        assertEquals("Reversed", details.getStatus().getCreditorTransactionStatusDisplayName());
        assertEquals(LocalDateTime.of(2026, 1, 31, 10, 30), details.getStatusDate());
        assertEquals("creditor_accounts", details.getAssociatedRecordType());
        assertEquals("99264300000001", details.getAssociatedRecordId());
        assertEquals("87654321", details.getAccountNumber());
        assertEquals("12345678", details.getDefendantAccountNumber());
        assertEquals(999L, details.getDefendantAccountId());
    }

    @Test
    void toOpal_mapsNullLegacyResponseToEmptyPayloadWithDefaultVersion() {
        GetMajorCreditorHistoryResponse result = mapper.toOpal(null);

        assertEquals(BigInteger.ONE, result.getVersion());
        assertNotNull(result.getPayload());
        assertEquals(List.of(), result.getPayload().getHistoryItems());
    }
}
