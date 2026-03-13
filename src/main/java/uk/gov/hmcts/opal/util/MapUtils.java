package uk.gov.hmcts.opal.util;

import java.util.LinkedHashMap;
import java.util.Map;

public final class MapUtils {

    private MapUtils() {
    }

    /**
     * Creates a map from key-value pairs, ensuring that keys are not null but allowing null values,
     * unlike {@link Map#of(Object, Object, Object, Object)}.
     *
     * @param keyValues the alternating key/value arguments used to populate the map.
     * @param <K> the map key type.
     * @param <V> the map value type.
     * @return a map containing the supplied key/value pairs in insertion order.
     * @throws IllegalArgumentException if the arguments are not supplied in key/value pairs, or if any key is null.
     */
    public static <K, V> Map<K, V> ofNullable(Object... keyValues) {
        if (keyValues.length % 2 != 0) {
            throw new IllegalArgumentException("Key/value arguments must be supplied in pairs.");
        }

        Map<K, V> map = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            if (keyValues[i] == null) {
                throw new IllegalArgumentException("Map keys must not be null.");
            }

            @SuppressWarnings("unchecked")
            K key = (K) keyValues[i];

            @SuppressWarnings("unchecked")
            V value = (V) keyValues[i + 1];

            map.put(key, value);
        }

        return map;
    }
}
