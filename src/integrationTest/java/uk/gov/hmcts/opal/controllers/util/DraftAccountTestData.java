package uk.gov.hmcts.opal.controllers.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import uk.gov.hmcts.opal.dto.ToJsonString;

/**
 * Consolidated test data for draft account requests. Contains inner records for POST (add), PUT (replace) and PATCH
 * (update) requests.
 */
public final class DraftAccountTestData {

    static final String MINIMAL_ACCOUNT_JSON = """
        {
          "account_type": "Fine",
          "defendant_type": "adultOrYouthOnly",
          "originator_name": "Test Court",
          "originator_id": 101,
          "originator_type": "NEW",
          "enforcement_court_id": 1,
          "payment_card_request": null,
          "account_sentence_date": "2026-05-12",
          "defendant": {
            "company_flag": false,
            "address_line_1": "1 Test Street"
          },
          "offences": [
            {
              "date_of_sentence": "2026-05-12",
              "offence_id": 290434,
              "impositions": [
                {
                  "result_id": "FVS",
                  "amount_imposed": 100.00,
                  "amount_paid": 0.00
                }
              ]
            }
          ],
          "payment_terms": {
            "payment_terms_type_code": "B"
          }
        }
        """;

    private DraftAccountTestData() {
    }

    // -------------------------------------------------------------------------
    // POST /draft-accounts
    // -------------------------------------------------------------------------

    public record Post(

        @JsonProperty("business_unit_id")
        int businessUnitId,

        @JsonProperty("submitted_by")
        String submittedBy,

        @JsonProperty("submitted_by_name")
        String submittedByName,

        @JsonProperty("account_type")
        String accountType,

        @JsonProperty("account_status")
        String accountStatus,

        @JsonRawValue
        @JsonProperty("account")
        String account

    ) implements ToJsonString {

        public static Post defaultData() {
            return new Post(
                77,
                "L077JG",
                "opal-test-post",
                "Fine",
                "Submitted",
                MINIMAL_ACCOUNT_JSON
            );
        }
    }

    // -------------------------------------------------------------------------
    // PUT /draft-accounts/{id}
    // -------------------------------------------------------------------------

    public record Put(

        @JsonProperty("business_unit_id")
        int businessUnitId,

        @JsonProperty("submitted_by")
        String submittedBy,

        @JsonProperty("submitted_by_name")
        String submittedByName,

        @JsonProperty("account_type")
        String accountType,

        @JsonProperty("account_status")
        String accountStatus,

        @JsonRawValue
        @JsonProperty("account")
        String account

    ) implements ToJsonString {

        public static Put defaultData() {
            return new Put(
                77,
                "L077JG",
                "opal-test-put",
                "Fine",
                "Resubmitted",
                MINIMAL_ACCOUNT_JSON
            );
        }
    }

    // -------------------------------------------------------------------------
    // PATCH /draft-accounts/{id}
    // -------------------------------------------------------------------------

    public record Patch(

        @JsonProperty("business_unit_id")
        int businessUnitId,

        @JsonProperty("account_status")
        String accountStatus,

        @JsonProperty("validated_by")
        String validatedBy,

        @JsonProperty("validated_by_name")
        String validatedByName,

        @JsonProperty("reason_text")
        String reasonText

    ) implements ToJsonString {

        public static Patch defaultData() {
            return new Patch(
                77,
                "Rejected",
                "L077JG",
                "opal-test-patch",
                "Feature toggle test"
            );
        }
    }
}
