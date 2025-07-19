package uk.gov.hmcts.opal;

public class SchemaPaths {

    public static final String DEFENDANT_ACCOUNT = "opal/defendant-account";
    public static final String DRAFT_ACCOUNT = "opal/draft-account";
    public static final String PAYMENT_TERMS = "opal/payment-terms";
    public static final String REFERENCE_DATA = "opal/reference-data";
    public static final String TIMELINE = "opal/timeline";

    // Draft Account Request Schemas
    public static final String ADD_DRAFT_ACCOUNT_REQUEST = DRAFT_ACCOUNT + "/addDraftAccountRequest.json";
    public static final String REPLACE_DRAFT_ACCOUNT_REQUEST = DRAFT_ACCOUNT + "/replaceDraftAccountRequest.json";
    public static final String UPDATE_DRAFT_ACCOUNT_REQUEST = DRAFT_ACCOUNT + "/updateDraftAccountRequest.json";

    public static final String GET_LJA_REF_DATA_RESPONSE = REFERENCE_DATA + "/getLJARefDataResponse.json";

    public static final String GET_RESULTS_REF_DATA_RESPONSE = REFERENCE_DATA + "/getResultsRefDataResponse.json";

    public static final String GET_PROSECUTORS_REF_DATA_RESPONSE = REFERENCE_DATA
        + "/getProsecutorsRefDataResponse.json";

    public static final String SEARCH_DEFENDANT_ACCOUNT = "searchDefendantAccount";

    private SchemaPaths() {
        // Utility class — prevent instantiation
    }
}
