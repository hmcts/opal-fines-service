package uk.gov.hmcts.opal.utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public final class JsonObjectUtils {

    private JsonObjectUtils() {
    }

    public static void addLongObjectIfPresent(JSONObject parent, Map<String, String> data, String sourceKey,
                                              String targetKey) throws JSONException {
        String value = data.get(sourceKey);
        if (value != null && !value.isBlank()) {
            parent.put(targetKey, new JSONObject().put(sourceKey, Long.parseLong(value)));
        }
    }

    public static void addIntObjectIfPresent(JSONObject parent, Map<String, String> data, String sourceKey,
                                             String targetKey) throws JSONException {
        String value = data.get(sourceKey);
        if (value != null && !value.isBlank()) {
            parent.put(targetKey, new JSONObject().put(sourceKey, Integer.parseInt(value)));
        }
    }
}
