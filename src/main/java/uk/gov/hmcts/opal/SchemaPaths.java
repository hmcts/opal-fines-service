package uk.gov.hmcts.opal;

public class SchemaPaths {

    public static final String DEFENDANT_ACCOUNT = "opal/defendant-account";
    public static final String DRAFT_ACCOUNT = "opal/draft-account";
    public static final String PAYMENT_TERMS = "opal/payment-terms";
    public static final String REFERENCE_DATA = "opal/reference-data";
    public static final String TIMELINE = "opal/timeline";
    public static final String ADD_DRAFT_ACCOUNT_REQUEST = DRAFT_ACCOUNT + "/addDraftAccountRequest.json";
    public static final String REPLACE_DRAFT_ACCOUNT_REQUEST = DRAFT_ACCOUNT + "/replaceDraftAccountRequest.json";
    public static final String UPDATE_DRAFT_ACCOUNT_REQUEST = DRAFT_ACCOUNT + "/updateDraftAccountRequest.json";

    public static final String GET_LJA_REF_DATA_RESPONSE = REFERENCE_DATA + "/getLJARefDataResponse.json";

    public static final String GET_RESULTS_REF_DATA_RESPONSE = REFERENCE_DATA + "/getResultsRefDataResponse.json";

    public static final String GET_PROSECUTORS_REF_DATA_RESPONSE = REFERENCE_DATA
        + "/getProsecutorsRefDataResponse.json";

    public static final String POST_DEFENDANT_ACCOUNT_ADD_PAYMENT_TERMS = DEFENDANT_ACCOUNT
        + "/addDefendantAccountPaymentTermsRequest.json";

    public static final String POST_DEFENDANT_ACCOUNT_SEARCH_RESPONSE = DEFENDANT_ACCOUNT
        + "/postDefendantAccountsSearchResponse.json";

    public static final String POST_DEFENDANT_ACCOUNT_SEARCH_REQUEST = DEFENDANT_ACCOUNT
        + "/postDefendantAccountsSearchRequest.json";

    public static final String PATCH_UPDATE_DEFENDANT_ACCOUNT_REQUEST = DEFENDANT_ACCOUNT
        + "/updateDefendantAccountRequest.json";

    public static final String PATCH_UPDATE_DEFENDANT_ACCOUNT_RESPONSE =
        DEFENDANT_ACCOUNT + "/updateDefendantAccountResponse.json";

    private SchemaPaths() {
        // Utility class — prevent instantiation
    }
}
