package uk.gov.hmcts.opal.mapper.request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigInteger;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.dto.UpdateDefendantAccountRequest;
import uk.gov.hmcts.opal.generated.model.CollectionOrderCommon;
import uk.gov.hmcts.opal.generated.model.UpdateDefendantAccountRequestPayload;

public class UpdateDefendantAccountRequestMapperTest {
    // The mapper is an interface with a default method, so we need to use the implementation class
    private final UpdateDefendantAccountRequestMapper mapper = new UpdateDefendantAccountRequestMapperImpl();

    @Test
    @DisplayName("numberToString returns null when input is null")
    void numberToString_null_returnsNull() {
        assertNull(mapper.numberToString(null),
            "Expected null input to yield null output");
    }

    @Test
    @DisplayName("numberToString converts integer and long correctly")
    void numberToString_integerAndLong_values() {
        assertEquals("42", mapper.numberToString(42));
        assertEquals("1234567890", mapper.numberToString(1234567890L));
    }

    @Test
    @DisplayName("numberToString truncates decimal portion for Double/Float")
    void numberToString_doubleAndFloat_truncated() {
        assertEquals("3", mapper.numberToString(3.99));
        assertEquals("9", mapper.numberToString(9.75f));
    }

    @Test
    @DisplayName("numberToString handles negative numbers")
    void numberToString_negative_values() {
        assertEquals("-5", mapper.numberToString(-5));
        assertEquals("-9", mapper.numberToString(-9.3));
    }

    @Test
    @DisplayName("collection order date defaults to today when flag is true and date is null")
    void toLegacyRequest_givenCollectionOrderTrueAndNullDate_thenDateDefaultsToToday() {
        UpdateDefendantAccountRequest request = UpdateDefendantAccountRequest.builder()
            .defendantAccountId(123L)
            .businessUnitId("17")
            .businessUnitUserId("user-1")
            .version(BigInteger.ONE)
            .payload(UpdateDefendantAccountRequestPayload.builder()
                .collectionOrder(CollectionOrderCommon.builder()
                    .collectionOrderFlag(true)
                    .collectionOrderDate((LocalDate) null)
                    .build())
                .build())
            .build();

        uk.gov.hmcts.opal.dto.legacy.LegacyUpdateDefendantAccountRequest legacy =
            mapper.toLegacyUpdateDefendantAccountRequest(request);

        assertNotNull(legacy.getCollectionOrder());
        assertEquals(Boolean.TRUE, legacy.getCollectionOrder().getCollectionOrderFlag());
        assertEquals(LocalDate.now(), legacy.getCollectionOrder().getCollectionOrderDate());
    }

    @Test
    @DisplayName("collection order date is cleared when flag is false")
    void toLegacyRequest_givenCollectionOrderFalseAndDate_thenDateIsCleared() {
        UpdateDefendantAccountRequest request = UpdateDefendantAccountRequest.builder()
            .defendantAccountId(123L)
            .businessUnitId("17")
            .businessUnitUserId("user-1")
            .version(BigInteger.ONE)
            .payload(UpdateDefendantAccountRequestPayload.builder()
                .collectionOrder(CollectionOrderCommon.builder()
                    .collectionOrderFlag(false)
                    .collectionOrderDate(LocalDate.of(2026, 3, 31))
                    .build())
                .build())
            .build();

        uk.gov.hmcts.opal.dto.legacy.LegacyUpdateDefendantAccountRequest legacy =
            mapper.toLegacyUpdateDefendantAccountRequest(request);

        assertNotNull(legacy.getCollectionOrder());
        assertEquals(Boolean.FALSE, legacy.getCollectionOrder().getCollectionOrderFlag());
        assertNull(legacy.getCollectionOrder().getCollectionOrderDate());
    }
}
