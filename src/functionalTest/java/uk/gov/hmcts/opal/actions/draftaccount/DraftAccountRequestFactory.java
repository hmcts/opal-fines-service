package uk.gov.hmcts.opal.actions.draftaccount;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static uk.gov.hmcts.opal.steps.BaseStepDef.addAllToJsonObject;
import static uk.gov.hmcts.opal.steps.BaseStepDef.addIntToJsonObject;
import static uk.gov.hmcts.opal.steps.BaseStepDef.addLongToJsonObject;
import static uk.gov.hmcts.opal.steps.BaseStepDef.addToJsonObject;
import static uk.gov.hmcts.opal.steps.BaseStepDef.addToJsonObjectOrNull;

/**
 * Builds draft-account request payloads for functional tests from shared JSON fixtures.
 */
public class DraftAccountRequestFactory {

    public static final String DEFAULT_ACCOUNT_PATH = "draftAccounts/accountJson/account.json";
    public static final String DEFAULT_TIMELINE_PATH = "draftAccounts/timelineJson/default.json";

    private static final String MANUAL_ACCOUNT_CREATION_RESOURCE_ROOT =
        "features/opalMode/manualAccountCreation/";

    /**
     * Defines how the `business_unit_id` field should be written into replace payloads.
     */
    public enum BusinessUnitIdMode {
        INTEGER,
        LONG
    }

    /**
     * Builds a create-draft-account request body from the supplied scenario data.
     *
     * @param dataToPost values to include in the request body.
     * @return request body for the create call.
     * @throws JSONException if the JSON payload cannot be created from the supplied values.
     * @throws IOException if a referenced fixture cannot be read.
     */
    public JSONObject buildCreateRequestBody(Map<String, String> dataToPost) throws JSONException, IOException {
        JSONObject postBody = new JSONObject();

        addLongToJsonObject(postBody, dataToPost, "business_unit_id");
        addAllToJsonObject(postBody, dataToPost, "submitted_by", "submitted_by_name", "account_type");
        addToJsonObjectOrNull(postBody, dataToPost, "account_status");
        postBody.put("account", loadAccountFixture(dataToPost.get("account")));
        return postBody;
    }

    /**
     * Builds the standard create-draft-account payload used by negative header/content-type tests.
     *
     * @param businessUnitId business-unit identifier to include in the payload.
     * @param submittedBy submitter identifier to include in the payload.
     * @return request body for the create call.
     * @throws JSONException if the JSON payload cannot be created from the supplied values.
     * @throws IOException if a referenced fixture cannot be read.
     */
    public JSONObject buildDefaultCreateRequestBody(String businessUnitId, String submittedBy)
        throws JSONException, IOException {
        return buildCreateRequestBody(
            Map.of(
                "business_unit_id", businessUnitId,
                "submitted_by", submittedBy,
                "account_type", "Fine",
                "account_status", "",
                "account", DEFAULT_ACCOUNT_PATH
            )
        );
    }

    /**
     * Builds a replace-draft-account request body from the supplied scenario data.
     *
     * @param dataToPost values to include in the request body.
     * @param businessUnitIdMode numeric mode to use for `business_unit_id`.
     * @return request body for the replace call.
     * @throws JSONException if the JSON payload cannot be created from the supplied values.
     * @throws IOException if a referenced fixture cannot be read.
     */
    public JSONObject buildReplaceRequestBody(Map<String, String> dataToPost, BusinessUnitIdMode businessUnitIdMode)
        throws JSONException, IOException {
        JSONObject postBody = new JSONObject();

        if (businessUnitIdMode == BusinessUnitIdMode.INTEGER) {
            addIntToJsonObject(postBody, dataToPost, "business_unit_id");
        } else {
            addLongToJsonObject(postBody, dataToPost, "business_unit_id");
        }

        addToJsonObjectOrNull(postBody, dataToPost, "submitted_by");
        addToJsonObject(postBody, dataToPost, "submitted_by_name");
        addToJsonObject(postBody, dataToPost, "account_type");
        addToJsonObjectOrNull(postBody, dataToPost, "account_status");
        postBody.put("account", loadAccountFixture(dataToPost.get("account")));
        return postBody;
    }

    /**
     * Loads and normalises the draft-account fixture identified by the supplied relative path.
     *
     * @param relativePath draft-account fixture path relative to the manual-account root.
     * @return parsed and normalised account JSON fixture.
     * @throws IOException if the fixture cannot be read.
     * @throws JSONException if the fixture does not contain valid JSON.
     */
    public JSONObject loadAccountFixture(String relativePath) throws IOException, JSONException {
        String resolvedPath = (relativePath == null || relativePath.isBlank()) ? DEFAULT_ACCOUNT_PATH : relativePath;
        JSONObject accountObject = new JSONObject(readResource(MANUAL_ACCOUNT_CREATION_RESOURCE_ROOT + resolvedPath));

        if (accountObject.has("originator_id")) {
            accountObject.put("originator_id", accountObject.getLong("originator_id"));
        }
        if (accountObject.has("enforcement_court_id")) {
            accountObject.put("enforcement_court_id", accountObject.getLong("enforcement_court_id"));
        }

        if (accountObject.has("offences")) {
            JSONArray offences = accountObject.getJSONArray("offences");
            for (int i = 0; i < offences.length(); i++) {
                JSONObject offence = offences.getJSONObject(i);
                if (offence.has("offence_id")) {
                    offence.put("offence_id", offence.getLong("offence_id"));
                }
            }
        }

        return accountObject;
    }

    /**
     * Loads the standard timeline fixture used by draft-account functional tests.
     *
     * @return parsed default timeline JSON array.
     * @throws IOException if the fixture cannot be read.
     * @throws JSONException if the fixture does not contain valid JSON.
     */
    public JSONArray loadDefaultTimelineFixture() throws IOException, JSONException {
        return new JSONArray(readResource(MANUAL_ACCOUNT_CREATION_RESOURCE_ROOT + DEFAULT_TIMELINE_PATH));
    }

    /**
     * Reads the supplied classpath resource as a UTF-8 string.
     *
     * @param resourcePath classpath resource path to load.
     * @return resource contents as a string.
     * @throws IOException if the resource cannot be found or read.
     */
    private String readResource(String resourcePath) throws IOException {
        try (InputStream inputStream = DraftAccountRequestFactory.class.getClassLoader()
            .getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
