package uk.gov.hmcts.opal.util;

import java.util.List;
import java.util.Objects;

public final class NumberUtils {

    private NumberUtils() {
        // utility
    }

    public static Long toLong(Integer value) {
        return value == null ? null : value.longValue();
    }

    public static List<Long> toLongList(List<Integer> values) {
        return values == null ? null : values.stream()
            .filter(Objects::nonNull)
            .map(Integer::longValue)
            .toList();
    }
}
