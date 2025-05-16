package uk.gov.hmcts.opal.steps;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class BaseStepDef {

    private static final String TEST_URL = System.getenv().getOrDefault("TEST_URL", "http://localhost:4550");

    protected static String getTestUrl() {
        return TEST_URL;
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
}
