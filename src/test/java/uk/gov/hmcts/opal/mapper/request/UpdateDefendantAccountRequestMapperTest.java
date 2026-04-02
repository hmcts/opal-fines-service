package uk.gov.hmcts.opal.mapper.request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.generated.model.CollectionOrderCommon;

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
    @DisplayName("collection order mapping populates today's date when flag is true and date is missing")
    void mapCollectionOrder_whenFlagTrueAndDateMissing_populatesToday() {
        CollectionOrderCommon src = CollectionOrderCommon.builder()
            .collectionOrderFlag(true)
            .build();

        var result = mapper.map(src);

        assertEquals(Boolean.TRUE, result.getCollectionOrderFlag());
        assertEquals(LocalDate.now(), result.getCollectionOrderDate());
    }

    @Test
    @DisplayName("collection order mapping clears date when flag is false")
    void mapCollectionOrder_whenFlagFalse_clearsDate() {
        CollectionOrderCommon src = CollectionOrderCommon.builder()
            .collectionOrderFlag(false)
            .collectionOrderDate(LocalDate.of(2025, 1, 1))
            .build();

        var result = mapper.map(src);

        assertEquals(Boolean.FALSE, result.getCollectionOrderFlag());
        assertNull(result.getCollectionOrderDate());
    }
}
