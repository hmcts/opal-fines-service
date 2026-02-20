package uk.gov.hmcts.opal.mapper.request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.dto.UpdateDefendantAccountRequest;

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
    @DisplayName("collectionOrder maps collection_order_date to LocalDate")
    void collectionOrder_mapsDateToLocalDate() {
        UpdateDefendantAccountRequest.CollectionOrderRequest src =
            UpdateDefendantAccountRequest.CollectionOrderRequest.builder()
                .collectionOrder(true)
                .collectionOrderDate("2025-01-01")
                .build();

        uk.gov.hmcts.opal.dto.legacy.common.CollectionOrder result = mapper.map(src);

        assertEquals(Boolean.TRUE, result.getCollectionOrderFlag());
        assertEquals(LocalDate.parse("2025-01-01"), result.getCollectionOrderDate());
        assertEquals(LocalDate.parse("2025-01-01"), mapper.stringToLocalDate("2025-01-01"));
    }
}
