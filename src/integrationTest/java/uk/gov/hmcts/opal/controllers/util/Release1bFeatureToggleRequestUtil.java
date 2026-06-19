package uk.gov.hmcts.opal.controllers.util;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import java.util.stream.Stream;
import org.junit.jupiter.params.provider.Arguments;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.RequestBuilder;

public final class Release1bFeatureToggleRequestUtil {

    private static final String AUTHORIZATION = "Bearer some_value";
    private static final String BUSINESS_UNIT_ID = "77";
    private static final String BUSINESS_UNIT_USER_ID = "FEATURE_TOGGLE_TEST";
    private static final String IF_MATCH = "\"0\"";
    private static final String DEFENDANT_ACCOUNT_ID = "999999";
    private static final String DEFENDANT_ACCOUNT_PARTY_ID = "999999";
    private static final String MAJOR_CREDITOR_ACCOUNT_ID = "10770000000041";
    private static final String MINOR_CREDITOR_ACCOUNT_ID = "999999";

    private Release1bFeatureToggleRequestUtil() {
    }

    public static Stream<Arguments> gatedRequests() {
        return Stream.of(
            Arguments.of(
                "Search Defendant Accounts",
                postJson("/defendant-accounts/search", """
                    {
                      "active_accounts_only": false,
                      "business_unit_ids": [77],
                      "reference_number": {
                        "organisation": false,
                        "account_number": "12345678",
                        "prosecutor_case_reference": null
                      },
                      "defendant": null
                    }
                    """)
            ),
            Arguments.of(
                "Get Defendant Account Header Summary",
                getWithAuthorization("/defendant-accounts/" + DEFENDANT_ACCOUNT_ID + "/header-summary")
            ),
            Arguments.of(
                "Get Defendant Account Party",
                getWithAuthorization(
                    "/defendant-accounts/" + DEFENDANT_ACCOUNT_ID
                        + "/defendant-account-parties/" + DEFENDANT_ACCOUNT_PARTY_ID
                )
            ),
            Arguments.of(
                "Get Defendant Account At A Glance",
                getWithAuthorization("/defendant-accounts/" + DEFENDANT_ACCOUNT_ID + "/at-a-glance")
            ),
            Arguments.of(
                "Get Major Creditor Account At A Glance",
                getWithAuthorization("/major-creditor-accounts/" + MAJOR_CREDITOR_ACCOUNT_ID + "/at-a-glance")
            ),
            Arguments.of(
                "Update Defendant Account",
                patchJsonWithBusinessHeaders("/defendant-accounts/" + DEFENDANT_ACCOUNT_ID, """
                    {
                      "comment_and_notes": {
                        "account_comment": "release-1b off-path check"
                      }
                    }
                    """)
            ),
            Arguments.of(
                "Add Note",
                post("/notes/add")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", AUTHORIZATION)
                    .header("If-Match", IF_MATCH)
                    .content("""
                        {
                          "activity_note": {
                            "record_type": "defendant_accounts",
                            "record_id": "77",
                            "note_text": "release-1b off-path check",
                            "note_type": "AA"
                          }
                        }
                        """)
            ),
            Arguments.of(
                "Replace Defendant Account Party",
                putJsonWithBusinessHeaders(
                    "/defendant-accounts/" + DEFENDANT_ACCOUNT_ID
                        + "/defendant-account-parties/" + DEFENDANT_ACCOUNT_PARTY_ID,
                    """
                    {
                      "defendant_account_party_type": "Defendant",
                      "is_debtor": true,
                      "party_details": {
                        "party_id": "999999",
                        "organisation_flag": true,
                        "organisation_details": {
                          "organisation_name": "Feature Toggle Test Ltd"
                        }
                      }
                    }
                    """
                )
            ),
            Arguments.of(
                "Add Defendant Account Party",
                postJsonWithBusinessHeaders(
                    "/defendant-accounts/" + DEFENDANT_ACCOUNT_ID + "/defendant-account-parties",
                    """
                    {
                      "defendant_account_party": {
                        "defendant_account_party_type": "Defendant",
                        "is_debtor": true,
                        "party_details": {
                          "party_id": "999999",
                          "organisation_flag": true,
                          "organisation_details": {
                            "organisation_name": "Feature Toggle Test Ltd"
                          }
                        },
                        "address": {
                          "address_line_1": "1 Test Street",
                          "address_line_2": null,
                          "address_line_3": null,
                          "address_line_4": null,
                          "address_line_5": null,
                          "postcode": "TE1 1ST"
                        },
                        "contact_details": null,
                        "vehicle_details": null,
                        "employer_details": null,
                        "language_preferences": null
                      }
                    }
                    """)
            ),
            Arguments.of(
                "Remove Defendant Account Party",
                deleteJsonWithBusinessHeaders(
                    "/defendant-accounts/" + DEFENDANT_ACCOUNT_ID
                        + "/defendant-account-parties/" + DEFENDANT_ACCOUNT_PARTY_ID,
                    """
                    {
                      "party_details": {
                        "party_id": "206"
                      }
                    }
                    """
                )
            ),
            Arguments.of(
                "Get Defendant Account Enforcement Status",
                getWithAuthorization("/defendant-accounts/" + DEFENDANT_ACCOUNT_ID + "/enforcement-status")
            ),
            Arguments.of(
                "Add Defendant Account Enforcement",
                postJsonWithBusinessHeaders("/defendant-accounts/" + DEFENDANT_ACCOUNT_ID + "/enforcements", """
                    {
                      "result_id": "CONF",
                      "enforcement_result_responses": [
                        {
                          "parameter_name": "amount_due",
                          "response": "100.00"
                        },
                        {
                          "parameter_name": "next_payment_date",
                          "response": "2026-01-15"
                        }
                      ],
                      "payment_terms": {
                        "days_in_default": 30,
                        "date_days_in_default_imposed": "2025-11-01",
                        "extension": true,
                        "reason_for_extension": "Financial hardship",
                        "payment_terms_type": {
                          "payment_terms_type_code": "B"
                        },
                        "effective_date": "2025-11-15",
                        "instalment_period": {
                          "instalment_period_code": "M"
                        },
                        "lump_sum_amount": 0.00,
                        "instalment_amount": 150.00,
                        "posted_details": {
                          "posted_date": "2025-11-02T10:30:00",
                          "posted_by": "System",
                          "posted_by_name": "System User"
                        }
                      }
                    }
                    """)
            ),
            Arguments.of(
                "Remove Defendant Account Enforcement Hold",
                patchJsonWithBusinessHeaders("/defendant-accounts/" + DEFENDANT_ACCOUNT_ID + "/remove-enf-hold", """
                    {
                      "reason": "remove hold reason"
                    }
                    """)
            ),
            Arguments.of(
                "Get Defendant Account Payment Terms",
                getWithAuthorization("/defendant-accounts/" + DEFENDANT_ACCOUNT_ID + "/payment-terms/latest")
            ),
            Arguments.of(
                "Add Defendant Account Payment Terms",
                postJsonWithBusinessHeaders("/defendant-accounts/" + DEFENDANT_ACCOUNT_ID + "/payment-terms", """
                    {
                      "payment_terms": {
                        "days_in_default": 30,
                        "date_days_in_default_imposed": "2025-11-05",
                        "extension": true,
                        "reason_for_extension": "extn reason text",
                        "effective_date": "2025-11-01",
                        "payment_terms_type": {
                          "payment_terms_type_code": "B",
                          "payment_terms_type_display_name": "By date"
                        },
                        "instalment_period": {
                          "instalment_period_code": "W",
                          "instalment_period_display_name": "Weekly"
                        },
                        "lump_sum_amount": 120.00,
                        "instalment_amount": 10.00,
                        "posted_details": {
                          "posted_by": "clerk1",
                          "posted_date": "2025-02-02T10:11:12",
                          "posted_by_name": "aa"
                        }
                      },
                      "request_payment_card": true,
                      "generate_payment_terms_change_letter": true
                    }
                    """)
            ),
            Arguments.of(
                "Add Defendant Account Payment Card Request",
                post("/defendant-accounts/" + DEFENDANT_ACCOUNT_ID + "/payment-card-request")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", AUTHORIZATION)
                    .header("Business-Unit-Id", BUSINESS_UNIT_ID)
                    .header("Business-Unit-User-Id", BUSINESS_UNIT_USER_ID)
                    .header("If-Match", IF_MATCH)
            ),
            Arguments.of(
                "Get Defendant Account Fixed Penalty",
                getWithAuthorization("/defendant-accounts/" + DEFENDANT_ACCOUNT_ID + "/fixed-penalty")
            ),
            Arguments.of(
                "Search Minor Creditor Accounts",
                postJson("/minor-creditor-accounts/search", """
                    {
                      "business_unit_ids": [77],
                      "active_accounts_only": false,
                      "account_number": "12345678"
                    }
                    """)
            ),
            Arguments.of(
                "Get Minor Creditor Account Header Summary",
                getWithAuthorization("/minor-creditor-accounts/" + MINOR_CREDITOR_ACCOUNT_ID + "/header-summary")
            ),
            Arguments.of(
                "Get Minor Creditor Account At A Glance",
                getWithAuthorization("/minor-creditor-accounts/" + MINOR_CREDITOR_ACCOUNT_ID + "/at-a-glance")
            ),
            Arguments.of(
                "Get Minor Creditor Account",
                getWithAuthorization("/minor-creditor-accounts/" + MINOR_CREDITOR_ACCOUNT_ID)
            ),
            Arguments.of(
                "Patch Minor Creditor Account",
                patchJsonWithBusinessHeaders("/minor-creditor-accounts/" + MINOR_CREDITOR_ACCOUNT_ID, """
                    {
                      "party_details": {
                        "party_id": "99008",
                        "organisation_flag": false,
                        "individual_details": {
                          "forenames": "Creditor",
                          "surname": "Updated"
                        }
                      },
                      "address": {
                        "address_line_1": "99 Updated Road",
                        "postcode": "NW1 1AA"
                      },
                      "payment": {
                        "hold_payment": true,
                        "pay_by_bacs": true
                      }
                    }
                    """)
            ),
            Arguments.of(
                "Get Result By Id",
                getWithAuthorization("/results/FVS")
            )
        );
    }

    private static RequestBuilder getWithAuthorization(String path) {
        return get(path).header("Authorization", AUTHORIZATION);
    }

    private static RequestBuilder postJson(String path, String body) {
        return post(path)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", AUTHORIZATION)
            .content(body);
    }

    private static RequestBuilder postJsonWithBusinessHeaders(String path, String body) {
        return post(path)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", AUTHORIZATION)
            .header("Business-Unit-Id", BUSINESS_UNIT_ID)
            .header("If-Match", IF_MATCH)
            .content(body);
    }

    private static RequestBuilder patchJsonWithBusinessHeaders(String path, String body) {
        return patch(path)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", AUTHORIZATION)
            .header("Business-Unit-Id", BUSINESS_UNIT_ID)
            .header("If-Match", IF_MATCH)
            .content(body);
    }

    private static RequestBuilder putJsonWithBusinessHeaders(String path, String body) {
        return put(path)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", AUTHORIZATION)
            .header("Business-Unit-Id", BUSINESS_UNIT_ID)
            .header("If-Match", IF_MATCH)
            .content(body);
    }

    private static RequestBuilder deleteJsonWithBusinessHeaders(String path, String body) {
        return delete(path)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", AUTHORIZATION)
            .header("Business-Unit-Id", BUSINESS_UNIT_ID)
            .header("If-Match", IF_MATCH)
            .content(body);
    }
}
