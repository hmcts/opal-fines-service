package uk.gov.hmcts.opal.steps;

import io.restassured.http.Header;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class BaseStepDef {

    private static final String TEST_URL = System.getenv().getOrDefault("TEST_URL", "http://localhost:4550");
    private static final String USER_SERVICE_URL = resolveUserServiceUrl();

    public static String getTestUrl() {
        return TEST_URL;
    }

    protected static String getUserServiceUrl() {
        return USER_SERVICE_URL;
    }

    private static String resolveUserServiceUrl() {
        String primary = System.getenv("OPAL_USER_SERVICE_API_URL");
        if (primary != null && !primary.isBlank()) {
            return primary;
        }
        String fallback = System.getenv("DEV_OPAL_USER_SERVICE_API_URL");
        if (fallback != null && !fallback.isBlank()) {
            return fallback;
        }
        return "http://localhost:4555";
    }

    public static JSONObject addToNewJsonObject(Map<String, String> dataToPost, String... names) throws JSONException {
        return addAllToJsonObject(new JSONObject(), dataToPost, names);
    }

    public static JSONObject addAllToJsonObject(JSONObject json, Map<String, String> dataToPost, String... names)
                                                                                            throws JSONException {
        for (String name: names) {
            addToJsonObject(json, dataToPost, name);
        }
        return json;
    }

    public static void addToJsonObject(JSONObject json, Map<String, String> dataToPost, String name)
                                                                                            throws JSONException {
        json.put(name, dataToPost.get(name) != null ? dataToPost.get(name) : "");
    }

    public static boolean dataExists(String data) {
        return data != null && !data.isBlank();
    }

    public static void addIntToJsonObject(JSONObject json, Map<String, String> dataToPatch, String key)
        throws JSONException {
        String data = dataToPatch.get(key);
        if (dataExists(data)) {
            json.put(key, Integer.parseInt(data));
        }
    }

    public static void addLongToJsonObject(JSONObject json, Map<String, String> dataToPatch, String key)
        throws JSONException {
        String data = dataToPatch.get(key);
        if (dataExists(data)) {
            json.put(key, Long.parseLong(data));
        }
    }

    public static void addIfPresentToJsonObject(JSONObject json, Map<String, String> dataToPatch, String key)
        throws JSONException {
        String data = dataToPatch.get(key);
        if (dataExists(data)) {
            json.put(key, data);
        }
    }

    public static void addToJsonObjectOrNull(JSONObject json, Map<String, String> data,
                                             String key) throws JSONException {
        String value = data.get(key);
        json.put(key, dataExists(value) ? value : JSONObject.NULL); // added here
    }

    public static Header createStringHeader(String headerName, Map<String, String> dataToPatch)
        throws JSONException {
        return new Header(headerName, dataToPatch.get(headerName));
    }

    public static Header createLongHeader(String headerName, Map<String, String> dataToPatch)
        throws JSONException {
        return new Header(headerName, checkLong(dataToPatch.get(headerName)));
    }

    public static Header createQuotedLongHeader(String headerName, Map<String, String> dataToPatch)
        throws JSONException {
        return new Header(headerName, "\"" + checkLong(dataToPatch.get(headerName)) + "\"");
    }

    private static String checkLong(String candidate) {
        return String.valueOf(Long.parseLong(candidate));
    }
}
