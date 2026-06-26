package uk.gov.hmcts.opal.util;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.opal.util.NumberUtils.toLong;
import static uk.gov.hmcts.opal.util.NumberUtils.toLongList;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class NumberUtilsTest {


    @Test
    void toLong_convertsIntegerToLong() {
        Long result = toLong(42);
        assertAll(
            () -> assertNotNull(result),
            () -> assertEquals(42L, result)
        );
    }

    @Test
    void toLongList_emptyReturnsEmptyList() {
        List<Long> result = toLongList(Collections.emptyList());
        assertAll(
            () -> assertNotNull(result),
            () -> assertTrue(result.isEmpty())
        );
    }

    @Test
    void toLongList_filtersNullsAndConverts() {
        List<Integer> input = Arrays.asList(1, null, 3);
        List<Long> result = toLongList(input);
        assertAll(
            () -> assertNotNull(result),
            () -> assertEquals(2, result.size()),
            () -> assertEquals(List.of(1L, 3L), result)
        );
    }
}

