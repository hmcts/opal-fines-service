package uk.gov.hmcts.opal.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@Slf4j(topic = "opal.OpalDefendantsPostPartyIntegrationTest")
@TestPropertySource(properties = {
    "launchdarkly.default-flag-values.release-1b=true"
})
class OpalDefendantsPostPartyIntegrationTest extends AbstractOpalDefendantsIntegrationTest {

    private static final long ACCOUNT_ID = 24010L;
    private static final String BU_ID = "78";
    private static final String URI_DEFENDANT_ACCOUNT_PARTIES
        = URL_BASE + "/" + ACCOUNT_ID + "/defendant-account-parties";


    private String validCommonFields() {
        return """
            "address": {
                "address_line_1": "1 Post Street",
                "address_line_2": null,
                "address_line_3": null,
                "address_line_4": null,
                "address_line_5": null,
                "postcode": "PO1 1ST"
            },
            "contact_details": null,
            "vehicle_details": null,
            "employer_details": null,
            "language_preferences": null
            """;
    }

    private String validIndividualDetails(String surname) {
        return """
            "party_details": {
                "organisation_flag": false,
                "individual_details": {
                    "title": "Mr",
                    "forenames": "John",
                    "surname": "%s",
                    "date_of_birth": null,
                    "age": null,
                    "national_insurance_number": null,
                    "individual_aliases": null
                }
            }
            """.formatted(surname);
    }

    @Test
    @DisplayName("POST Add DAP - 404 when account not in header BU")
    @JiraStory("PO-1896")
    @JiraEpic("PO-1970")
    @JiraTestKey("PO-6055")
    void post_notFound_whenAccountNotInHeaderBU() throws Exception {
        Integer currentVersion = versionFor(ACCOUNT_ID);
        userStateStub.addPermissions((short)99, FinesPermission.values());

        HttpHeaders headers = buildHttpHeaders("99", "\"" + currentVersion + "\"");

        String body = """
            {
                "defendant_account_party": {
                    "defendant_account_party_type": "Defendant",
                    "is_debtor": false,
                    "party_details": {
                        "organisation_flag": true,
                        "organisation_details": { "organisation_name": "PostCo Ltd" }
                    },
                    %s
                }
            }
            """.formatted(validCommonFields());

        ResultActions res = mockMvc.perform(
            post(URI_DEFENDANT_ACCOUNT_PARTIES)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        );

        log.info("POST DAP wrong BU resp:\n{}", res.andReturn().getResponse().getContentAsString());

        res.andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/entity-not-found"));
    }

    @Test
    @DisplayName("POST Add DAP - happy path: organisation, non-debtor, bumps version")
    @JiraStory("PO-1896")
    @JiraEpic("PO-1970")
    @JiraTestKey("PO-6059")
    void post_happyPath_organisation_isDebtorFalse() throws Exception {
        Integer currentVersion = versionFor(ACCOUNT_ID);
        String etag = "\"" + currentVersion + "\"";

        HttpHeaders headers = buildHttpHeaders(BU_ID, etag);

        String body = """
            {
                "defendant_account_party": {
                    "defendant_account_party_type": "Defendant",
                    "is_debtor": false,
                    "party_details": {
                        "organisation_flag": true,
                        "organisation_details": { "organisation_name": "PostCo Ltd" }
                    },
                    %s
                }
            }
            """.formatted(validCommonFields());

        ResultActions call = mockMvc.perform(
            post(URI_DEFENDANT_ACCOUNT_PARTIES)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        );

        String resp = call.andReturn().getResponse().getContentAsString();
        log.info("POST DAP happy path (org, no debtor) resp:\n{}", ToJsonString.toPrettyJson(resp));

        String expectedNextEtag = "\"" + (currentVersion + 1) + "\"";

        call.andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.ETAG, expectedNextEtag))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.defendant_account_party.defendant_account_party_type").value("Defendant"))
            .andExpect(jsonPath("$.defendant_account_party.is_debtor").value(false))
            .andExpect(jsonPath("$.defendant_account_party.party_details.organisation_details.organisation_name")
                .value("PostCo Ltd"));

        Integer updatedVersion = versionFor(ACCOUNT_ID);
        assertEquals(currentVersion + 1, updatedVersion);
    }

    @Test
    @DisplayName("POST Add DAP - happy path: individual debtor creates debtor_detail row")
    @JiraStory("PO-1896")
    @JiraEpic("PO-1970")
    @JiraTestKey("PO-6057")
    void post_happyPath_individual_isDebtorTrue_createsDebtorDetail() throws Exception {
        Integer currentVersion = versionFor(ACCOUNT_ID);
        String etag = "\"" + currentVersion + "\"";

        HttpHeaders headers = buildHttpHeaders(BU_ID, etag);

        String body = """
            {
                "defendant_account_party": {
                    "defendant_account_party_type": "Parent/Guardian",
                    "is_debtor": true,
                    %s,
                    "address": {
                        "address_line_1": "99 Test Road",
                        "address_line_2": null,
                        "address_line_3": null,
                        "address_line_4": null,
                        "address_line_5": null,
                        "postcode": "TE1 1ST"
                    },
                    "contact_details": null,
                    "vehicle_details": {
                        "vehicle_make_and_model": "Ford Focus",
                        "vehicle_registration": "PO11 TST"
                    },
                    "employer_details": {
                        "employer_name": "Post Corp",
                        "employer_reference": null,
                        "employer_email_address": null,
                        "employer_telephone_number": null,
                        "employer_address": {
                            "address_line_1": "10 Corp Ave",
                            "address_line_2": null,
                            "address_line_3": null,
                            "address_line_4": null,
                            "address_line_5": null,
                            "postcode": "CO1 1RP"
                        }
                    },
                    "language_preferences": {
                        "document_language_preference": {
                            "language_code": "EN",
                            "language_display_name": "English only"
                        },
                        "hearing_language_preference": {
                            "language_code": "CY",
                            "language_display_name": "Welsh and English"
                        }
                    }
                }
            }
            """.formatted(validIndividualDetails("PostTest"));

        ResultActions call = mockMvc.perform(
            post(URI_DEFENDANT_ACCOUNT_PARTIES)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        );

        String resp = call.andReturn().getResponse().getContentAsString();
        log.info("POST DAP happy path (individual debtor) resp:\n{}", ToJsonString.toPrettyJson(resp));

        String expectedNextEtag = "\"" + (currentVersion + 1) + "\"";

        call.andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.ETAG, expectedNextEtag))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.defendant_account_party.defendant_account_party_type").value("Parent/Guardian"))
            .andExpect(jsonPath("$.defendant_account_party.is_debtor").value(true))
            .andExpect(jsonPath("$.defendant_account_party.party_details.individual_details.surname")
                .value("PostTest"));

        String newPartyId = com.jayway.jsonpath.JsonPath.read(resp,
            "$.defendant_account_party.party_details.party_id");
        assertNotNull(newPartyId);

        Map<String, Object> debtorRow = jdbcTemplate.queryForMap(
            "SELECT party_id, vehicle_make, vehicle_registration, employer_name, employer_address_line_1, "
                + "document_language FROM debtor_detail WHERE party_id = ?",
            Long.parseLong(newPartyId));

        assertEquals(Long.parseLong(newPartyId), ((Number) debtorRow.get("party_id")).longValue());
        assertEquals("Ford Focus", debtorRow.get("vehicle_make"));
        assertEquals("PO11 TST", debtorRow.get("vehicle_registration"));
        assertEquals("Post Corp", debtorRow.get("employer_name"));
        assertEquals("10 Corp Ave", debtorRow.get("employer_address_line_1"));
        assertEquals("EN", debtorRow.get("document_language"));

        Integer updatedVersion = versionFor(ACCOUNT_ID);
        assertEquals(currentVersion + 1, updatedVersion);
    }

    @Test
    @DisplayName("POST Add DAP - stale If-Match returns 409 conflict")
    @JiraStory("PO-1896")
    @JiraEpic("PO-1970")
    @JiraTestKey("PO-6058")
    void post_conflict_whenIfMatchIsStale() throws Exception {
        HttpHeaders headers = buildHttpHeaders(BU_ID, "\"9999999\"");

        String body = """
            {
                "defendant_account_party": {
                    "defendant_account_party_type": "Defendant",
                    "is_debtor": false,
                    "party_details": {
                        "organisation_flag": true,
                        "organisation_details": { "organisation_name": "StaleCo Ltd" }
                    },
                    %s
                }
            }
            """.formatted(validCommonFields());

        ResultActions res = mockMvc.perform(
            post(URI_DEFENDANT_ACCOUNT_PARTIES)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        );

        log.info("POST DAP conflict resp:\n{}", res.andReturn().getResponse().getContentAsString());

        res.andExpect(status().isConflict())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    @DisplayName("POST Add DAP - two sequential calls succeed and each bumps version")
    @JiraStory("PO-1896")
    @JiraEpic("PO-1970")
    @JiraTestKey("PO-6054")
    void post_twoSequentialParties_eachBumpsVersion() throws Exception {
        Integer versionBefore = versionFor(ACCOUNT_ID);

        for (int i = 1; i <= 2; i++) {
            Integer currentVersion = versionFor(ACCOUNT_ID);
            String etag = "\"" + currentVersion + "\"";

            HttpHeaders headers = buildHttpHeaders(BU_ID, etag);

            String body = """
                {
                    "defendant_account_party": {
                        "defendant_account_party_type": "Defendant",
                        "is_debtor": false,
                        "party_details": {
                            "organisation_flag": true,
                            "organisation_details": { "organisation_name": "SeqPostCo %d" }
                        },
                        %s
                    }
                }
                """.formatted(i, validCommonFields());

            ResultActions call = mockMvc.perform(
                post(URI_DEFENDANT_ACCOUNT_PARTIES)
                    .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
            );

            call.andExpect(status().isOk());
        }

        Integer versionAfter = versionFor(ACCOUNT_ID);
        assertEquals(versionBefore + 2, versionAfter);

        Integer partyCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM defendant_account_parties dap "
                + "JOIN parties p ON p.party_id = dap.party_id "
                + "WHERE dap.defendant_account_id = ? "
                + "AND p.organisation_name IN ('SeqPostCo 1', 'SeqPostCo 2')",
            Integer.class, ACCOUNT_ID);
        assertNotNull(partyCount);
        assertEquals(2, partyCount);
    }

    @Test
    @DisplayName("POST Add DAP - is_debtor true without details creates debtor row with null fields")
    @JiraStory("PO-1896")
    @JiraEpic("PO-1970")
    @JiraTestKey("PO-6056")
    void post_isDebtor_true_noDetails_createsDebtorRow_withNulls() throws Exception {
        Integer currentVersion = versionFor(ACCOUNT_ID);
        String etag = "\"" + currentVersion + "\"";

        HttpHeaders headers = buildHttpHeaders(BU_ID, etag);

        String body = """
            {
                "defendant_account_party": {
                    "defendant_account_party_type": "Defendant",
                    "is_debtor": true,
                    %s,
                    %s
                }
            }
            """.formatted(validIndividualDetails("Minimal"), validCommonFields());

        ResultActions call = mockMvc.perform(
            post(URI_DEFENDANT_ACCOUNT_PARTIES)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        );

        String resp = call.andReturn().getResponse().getContentAsString();
        log.info("POST DAP (debtor minimal) resp:\n{}", ToJsonString.toPrettyJson(resp));

        call.andExpect(status().isOk())
            .andExpect(jsonPath("$.defendant_account_party.is_debtor").value(true));

        String newPartyId = com.jayway.jsonpath.JsonPath.read(resp,
            "$.defendant_account_party.party_details.party_id");
        assertNotNull(newPartyId);

        Integer debtorRowCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM debtor_detail WHERE party_id = ?",
            Integer.class, Long.parseLong(newPartyId));
        assertEquals(1, debtorRowCount);

        Map<String, Object> debtorRow = jdbcTemplate.queryForMap(
            "SELECT vehicle_make, vehicle_registration, employer_name FROM debtor_detail WHERE party_id = ?",
            Long.parseLong(newPartyId));

        assertNull(debtorRow.get("vehicle_make"));
        assertNull(debtorRow.get("vehicle_registration"));
        assertNull(debtorRow.get("employer_name"));
    }

    @Test
    @DisplayName("POST Add DAP - response validates against schema")
    @JiraStory("PO-1896")
    @JiraEpic("PO-1970")
    @JiraTestKey("PO-6060")
    void post_response_validatesAgainstSchema() throws Exception {
        Integer currentVersion = versionFor(ACCOUNT_ID);
        String etag = "\"" + currentVersion + "\"";

        HttpHeaders headers = buildHttpHeaders(BU_ID, etag);

        String body = """
            {
                "defendant_account_party": {
                    "defendant_account_party_type": "Defendant",
                    "is_debtor": true,
                    %s,
                    "address": {
                        "address_line_1": "Schema Street",
                        "address_line_2": null,
                        "address_line_3": null,
                        "address_line_4": null,
                        "address_line_5": null,
                        "postcode": "SC1 1MA"
                    },
                    "contact_details": null,
                    "vehicle_details": {
                        "vehicle_make_and_model": "Schema Car",
                        "vehicle_registration": "SC11 EMA"
                    },
                    "employer_details": {
                        "employer_name": "Schema Employer",
                        "employer_reference": null,
                        "employer_email_address": null,
                        "employer_telephone_number": null,
                        "employer_address": {
                            "address_line_1": "Schema House",
                            "address_line_2": null,
                            "address_line_3": null,
                            "address_line_4": null,
                            "address_line_5": null,
                            "postcode": "SC2 2MA"
                        }
                    },
                    "language_preferences": {
                        "document_language_preference": {
                            "language_code": "EN",
                            "language_display_name": "English only"
                        },
                        "hearing_language_preference": {
                            "language_code": "CY",
                            "language_display_name": "Welsh and English"
                        }
                    }
                }
            }
            """.formatted(validIndividualDetails("Schema"));

        ResultActions call = mockMvc.perform(
            post(URI_DEFENDANT_ACCOUNT_PARTIES)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        );

        String resp = call.andReturn().getResponse().getContentAsString();
        log.info("POST DAP schema validation resp:\n{}", ToJsonString.toPrettyJson(resp));

        call.andExpect(status().isOk());

        jsonSchemaValidationService.validateOrError(resp, DEFENDANT_PARTY_RESPONSE_SCHEMA);
    }
}
