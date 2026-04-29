package uk.gov.hmcts.opal.utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Provides small helpers for adding typed nested JSON objects when optional scenario values are
 * present.
 */
public final class JsonObjectUtils {

    /**
     * Utility class.
     */
    private JsonObjectUtils() {
    }

    /**
     * Adds a nested JSON object containing a long-valued field when the source value is present.
     *
     * @param parent parent JSON object to update.
     * @param data scenario values keyed by field name.
     * @param sourceKey source field name containing the numeric value.
     * @param targetKey target field name used for the nested object.
     * @throws JSONException if the JSON object cannot be updated.
     */
    public static void addLongObjectIfPresent(JSONObject parent, Map<String, String> data, String sourceKey,
                                              String targetKey) throws JSONException {
        String value = data.get(sourceKey);
        if (value != null && !value.isBlank()) {
            parent.put(targetKey, new JSONObject().put(sourceKey, Long.parseLong(value)));
        }
    }

    /**
     * Adds a nested JSON object containing an integer-valued field when the source value is
     * present.
     *
     * @param parent parent JSON object to update.
     * @param data scenario values keyed by field name.
     * @param sourceKey source field name containing the numeric value.
     * @param targetKey target field name used for the nested object.
     * @throws JSONException if the JSON object cannot be updated.
     */
    public static void addIntObjectIfPresent(JSONObject parent, Map<String, String> data, String sourceKey,
                                             String targetKey) throws JSONException {
        String value = data.get(sourceKey);
        if (value != null && !value.isBlank()) {
            parent.put(targetKey, new JSONObject().put(sourceKey, Integer.parseInt(value)));
        }
    }
}
