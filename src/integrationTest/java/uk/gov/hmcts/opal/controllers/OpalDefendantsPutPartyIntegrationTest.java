package uk.gov.hmcts.opal.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.dto.ToJsonString;

@Slf4j(topic = "opal.OpalDefendantsPutPartyIntegrationTest")
class OpalDefendantsPutPartyIntegrationTest extends AbstractOpalDefendantsIntegrationTest {

    @Test
    @DisplayName("OPAL: PUT Replace DAP – Not Found (account not in BU)")
    void put_notFound_whenAccountNotInHeaderBU() throws Exception {
        authoriseAllPermissions();

        Integer currentVersion = versionFor(20010L);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("good_token");
        headers.add("Business-Unit-Id", "99");
        headers.add(HttpHeaders.IF_MATCH, "\"" + currentVersion + "\"");

        String body = """
            {
                "defendant_account_party_type": "Defendant",
                "is_debtor": true,
                "party_details": {
                "party_id": "20010",
                "organisation_flag": true,
                "organisation_details": { "organisation_name": "PutCo Ltd" }
                }
            }
            """;

        ResultActions res = mockMvc.perform(
            put("/defendant-accounts/20010/defendant-account-parties/20010")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        );

        log.info("PUT DAP wrong BU resp:\n{}", res.andReturn().getResponse().getContentAsString());

        res.andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/entity-not-found"));
    }

    @Test
    @DisplayName("OPAL: PUT Replace DAP – Happy path (updates party + debtor + bumps version)")
    void put_happyPath_updates_andReturnsResponse() throws Exception {
        authoriseAllPermissions();

        Integer currentVersion = versionFor(20010L);
        String etag = "\"" + currentVersion + "\"";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("good_token");
        headers.add("Business-Unit-Id", "78");
        headers.add(HttpHeaders.IF_MATCH, etag);

        String body = """
            {
                "defendant_account_party_type": "Defendant",
                "is_debtor": true,
                "party_details": {
                 "party_id": "20010",
                 "organisation_flag": true,
                  "organisation_details": { "organisation_name": "PutCo Ltd" }
                },
                "address": {
                "address_line_1": "100 Put Street",
                "postcode": "PU1 1TT"
                },
                    "contact_details": {
                    "primary_email_address": "putco@example.com",
                    "work_telephone_number": "0207000000"
                },
                    "vehicle_details": {
                    "vehicle_make_and_model": "VW Golf",
                    "vehicle_registration": "PU70ABC"
                },
                    "employer_details": {
                    "employer_name": "Put Employer",
                    "employer_address": {
                        "address_line_1": "1 Employer Way",
                        "postcode": "EM1 1AA"
                    }
                },
                    "language_preferences": {
                    "document_language_preference": { "language_code": "EN" },
                    "hearing_language_preference":  { "language_code": "CY" }
                 }
            }
            """;

        ResultActions call = mockMvc.perform(
            put("/defendant-accounts/20010/defendant-account-parties/20010")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        );

        String resp = call.andReturn().getResponse().getContentAsString();
        log.info("PUT DAP happy path resp:\n{}", resp);

        String expectedNextEtag = "\"" + (currentVersion + 1) + "\"";

        call.andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.ETAG, expectedNextEtag))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.defendant_account_party.defendant_account_party_type").value("Defendant"));
    }

    @Test
    @DisplayName("OPAL: PUT Replace DAP – DAP not found on account")
    void put_notFound_whenDapMissing() throws Exception {
        authoriseAllPermissions();

        Integer currentVersion = versionFor(20010L);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("good_token");
        headers.add("Business-Unit-Id", "78");
        headers.add(HttpHeaders.IF_MATCH, "\"" + currentVersion + "\"");

        String body = """
            {
                    "defendant_account_party_type": "Defendant",
                    "party_details": {
                    "party_id": "20010",
                    "organisation_flag": true,
                    "organisation_details": { "organisation_name": "StillCo" }
                 }
            }
            """;

        ResultActions res = mockMvc.perform(
            put("/defendant-accounts/20010/defendant-account-parties/99999")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        );

        log.info("PUT DAP missing DAP resp:\n{}", res.andReturn().getResponse().getContentAsString());

        res.andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/entity-not-found"));
    }

    @Test
    @DisplayName("OPAL: PUT Replace DAP – Individual aliases upsert/trim on isolated IDs (22004)")
    void put_individual_aliases_upsert_and_trim() throws Exception {
        authoriseAllPermissions();

        Integer currentVersion = versionFor(22004L);
        String etag = "\"" + currentVersion + "\"";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("good_token");
        headers.add("Business-Unit-Id", "78");
        headers.add(HttpHeaders.IF_MATCH, etag);

        String body = """
            {
              "defendant_account_party_type": "Defendant",
              "is_debtor": false,
              "party_details": {
                "party_id": "22004",
                "organisation_flag": false,
                "individual_details": {
                  "forenames": "MainForenames",
                  "surname": "MainSurname",
                  "individual_aliases": [
                    { "alias_id": "2200401", "sequence_number": 1, "forenames": "Jane", "surname": "Doe" },
                    { "alias_id": "","sequence_number": 2, "forenames": "J.", "surname": "Smith" }
                  ]
                }
              }
            }
            """;

        ResultActions call = mockMvc.perform(
            put("/defendant-accounts/22004/defendant-account-parties/22004").headers(headers)
                .contentType(MediaType.APPLICATION_JSON).content(body));

        String resp = call.andReturn().getResponse().getContentAsString();
        log.info("PUT DAP individual aliases (22004) resp:\n{}", resp);

        String expectedNextEtag = "\"" + (currentVersion + 1) + "\"";

        call.andExpect(status().isOk()).andExpect(header().string(HttpHeaders.ETAG, expectedNextEtag))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.defendant_account_party.defendant_account_party_type").value("Defendant"));

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
            "SELECT alias_id, sequence_number, forenames, surname, organisation_name "
                + "FROM aliases WHERE party_id = ? ORDER BY sequence_number", 22004L);

        assertEquals(2, rows.size());
        assertEquals(1, ((Number) rows.get(0).get("sequence_number")).intValue());
        assertEquals(2200401L, ((Number) rows.get(0).get("alias_id")).longValue());
        assertEquals("Jane", rows.get(0).get("forenames"));
        assertEquals("Doe", rows.get(0).get("surname"));
        assertNull(rows.get(0).get("organisation_name"));

        assertEquals(2, ((Number) rows.get(1).get("sequence_number")).intValue());
        assertNotNull(rows.get(1).get("alias_id"));
        assertNotEquals(2200401L, ((Number) rows.get(1).get("alias_id")).longValue());
        assertEquals("J.", rows.get(1).get("forenames"));
        assertEquals("Smith", rows.get(1).get("surname"));
        assertNull(rows.get(1).get("organisation_name"));

        Integer updatedVersion = versionFor(22004L);
        assertEquals(currentVersion + 1, updatedVersion);
    }

    @Test
    @Sql(scripts = "classpath:db/insertData/update_into_parties.sql", executionPhase = BEFORE_TEST_METHOD)
    @DisplayName("OPAL: PUT Replace DAP – Organisation aliases upsert (update + insert) and trim (delete missing)")
    void put_org_aliases_upsert_and_trim() throws Exception {
        authoriseAllPermissions();

        Integer currentVersion = versionFor(20010L);
        assertNotNull(currentVersion);
        String etag = "\"" + currentVersion + "\"";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("good_token");
        headers.add("Business-Unit-Id", "78");
        headers.add(HttpHeaders.IF_MATCH, etag);

        String body = """
            {
              "defendant_account_party_type": "Defendant",
              "is_debtor": true,
              "party_details": {
                "party_id": "20010",
                "organisation_flag": true,
                "organisation_details": {
                  "organisation_name": "PutCo Ltd",
                  "organisation_aliases": [
                    { "alias_id": 200101, "sequence_number": 2, "organisation_name": "PutCo Alias One (updated)" },
                    { "sequence_number": 3, "organisation_name": "PutCo Alias Three (new)" }
                  ]
                }
              },
              "address": { "address_line_1": "100 Put Street", "postcode": "PU1 1TT" },
              "contact_details": { "primary_email_address": "putco@example.com",
              "work_telephone_number": "0207000000" },
              "vehicle_details": { "vehicle_make_and_model": "VW Golf", "vehicle_registration": "PU70ABC" },
              "employer_details": {
                "employer_name": "Put Employer",
                "employer_address": { "address_line_1": "1 Employer Way", "postcode": "EM1 1AA" }
              },
              "language_preferences": {
                "document_language_preference": { "language_code": "EN" },
                "hearing_language_preference":  { "language_code": "CY" }
              }
            }
            """;

        ResultActions call = mockMvc.perform(
            put("/defendant-accounts/20010/defendant-account-parties/20010").headers(headers)
                .contentType(MediaType.APPLICATION_JSON).content(body));

        String resp = call.andReturn().getResponse().getContentAsString();
        log.info("PUT DAP (org aliases upsert/trim) resp:\n{}", resp);

        String expectedNextEtag = "\"" + (currentVersion + 1) + "\"";
        call.andExpect(status().isOk()).andExpect(header().string(HttpHeaders.ETAG, expectedNextEtag))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.defendant_account_party.defendant_account_party_type").value("Defendant"));

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
            "SELECT alias_id, sequence_number, organisation_name, surname, forenames "
                + "FROM aliases WHERE party_id = ? ORDER BY sequence_number ASC", 20010L);

        assertEquals(2, rows.size(), "Expected exactly 2 aliases after upsert+trim");

        Map<String, Object> r0 = rows.get(0);
        assertEquals(2, ((Number) r0.get("sequence_number")).intValue());
        assertEquals(200101L, ((Number) r0.get("alias_id")).longValue());
        assertEquals("PutCo Alias One (updated)", r0.get("organisation_name"));
        assertNull(r0.get("surname"));
        assertNull(r0.get("forenames"));

        Map<String, Object> r1 = rows.get(1);
        assertEquals(3, ((Number) r1.get("sequence_number")).intValue());
        Long newId = ((Number) r1.get("alias_id")).longValue();
        assertNotNull(newId);
        assertNotEquals(200102L, newId, "Omitted alias 200102 must have been deleted");
        assertEquals("PutCo Alias Three (new)", r1.get("organisation_name"));
        assertNull(r1.get("surname"));
        assertNull(r1.get("forenames"));

        Integer stillThere =
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM aliases WHERE party_id = ? AND alias_id = 200102",
                Integer.class, 20010L);

        assertEquals(0, stillThere, "Alias 200102 should be trimmed");

        Integer updatedVersion = versionFor(20010L);
        assertEquals(currentVersion + 1, updatedVersion);
    }

    @Test
    @Sql(scripts = "classpath:db/insertData/update_into_parties.sql", executionPhase = BEFORE_TEST_METHOD)
    @DisplayName("OPAL: PUT Replace DAP – is_debtor = false -> clear debtor fields but do not delete row")
    void put_replace_dap_isDebtorFalse_clearsDebtorFieldsButKeepsRow() throws Exception {
        authoriseAllPermissions();

        Integer currentVersion = versionFor(20010L);
        assertNotNull(currentVersion);
        String etag = "\"" + currentVersion + "\"";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("good_token");
        headers.add("Business-Unit-Id", "78");
        headers.add(HttpHeaders.IF_MATCH, etag);

        String body = """
                {
                "defendant_account_party_type": "Defendant",
                "is_debtor": false,
                "party_details": {
                "party_id": "20010",
                    "organisation_flag": true,
                    "organisation_details": {
                    "organisation_name": "PutCo Ltd",
                        "organisation_aliases": [
                    { "alias_id": 200101, "sequence_number": 2, "organisation_name": "PutCo Alias One (updated)" },
                    { "sequence_number": 3, "organisation_name": "PutCo Alias Three (new)" }
                      ]
                }
            },
                "address": { "address_line_1": "100 Put Street", "postcode": "PU1 1TT" },
                "contact_details": {},
                "vehicle_details": {},
                "employer_details": {},
                "language_preferences": {}
            }
            """;

        ResultActions call = mockMvc.perform(
            put("/defendant-accounts/20010/defendant-account-parties/20010")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

        String resp = call.andReturn().getResponse().getContentAsString();
        log.info("PUT DAP (is_debtor=false) resp:\n{}", ToJsonString.toPrettyJson(resp));

        String expectedNextEtag = "\"" + (currentVersion + 1) + "\"";
        call.andExpect(status().isOk()).andExpect(header().string(HttpHeaders.ETAG, expectedNextEtag))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.defendant_account_party.defendant_account_party_type").value("Defendant"));

        Map<String, Object> row = jdbcTemplate.queryForMap(
            "SELECT party_id, vehicle_make, vehicle_registration, employer_name, document_language "
                + "FROM debtor_detail WHERE party_id = ?",
            20010L);

        assertEquals(20010L, ((Number) row.get("party_id")).longValue());
        assertNull(row.get("vehicle_make"), "vehicle_make should be cleared to null");
        assertNull(row.get("vehicle_registration"), "vehicle_registration should be cleared to null");
        assertNull(row.get("employer_name"), "employer_name should be cleared to null");
        assertNull(row.get("document_language"), "document_language should be cleared to null");

        Integer updatedVersion = versionFor(20010L);
        assertEquals(currentVersion + 1, updatedVersion);
    }

    @Test
    @Sql(scripts = "classpath:db/insertData/update_into_parties.sql", executionPhase = BEFORE_TEST_METHOD)
    @DisplayName("OPAL: PUT Replace DAP – is_debtor = true -> upsert debtor details (create/update)")
    void put_replace_dap_isDebtorTrue_upsertsDebtorDetails() throws Exception {
        authoriseAllPermissions();

        Integer currentVersion = versionFor(20010L);
        assertNotNull(currentVersion);
        String etag = "\"" + currentVersion + "\"";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("good_token");
        headers.add("Business-Unit-Id", "78");
        headers.add(HttpHeaders.IF_MATCH, etag);

        String body = """
                {
                "defendant_account_party_type": "Defendant",
                "is_debtor": true,
                "party_details": {
                "party_id": "20010",
                    "organisation_flag": true,
                    "organisation_details": {
                    "organisation_name": "PutCo Ltd",
                        "organisation_aliases": [
                    { "alias_id": 200101, "sequence_number": 2, "organisation_name": "PutCo Alias One (updated)" },
                    { "sequence_number": 3, "organisation_name": "PutCo Alias Three (new)" }
                      ]
                }
            },
                "address": { "address_line_1": "100 Put Street", "postcode": "PU1 1TT" },
                "contact_details": { "primary_email_address": "putco@example.com",
                "work_telephone_number": "0207000000" },
                "vehicle_details": { "vehicle_make_and_model": "VW Golf", "vehicle_registration": "PU70ABC" },
                "employer_details": {
                "employer_name": "Put Employer",
                    "employer_address": { "address_line_1": "1 Employer Way", "postcode": "EM1 1AA" }
            },
                "language_preferences": {
                "document_language_preference": { "language_code": "EN" },
                "hearing_language_preference":  { "language_code": "CY" }
            }
            }
            """;

        ResultActions call = mockMvc.perform(
            put("/defendant-accounts/20010/defendant-account-parties/20010").headers(headers)
                .contentType(MediaType.APPLICATION_JSON).content(body));

        String resp = call.andReturn().getResponse().getContentAsString();
        log.info("PUT DAP (is_debtor=true) resp:\n{}", resp);

        String expectedNextEtag = "\"" + (currentVersion + 1) + "\"";
        call.andExpect(status().isOk()).andExpect(header().string(HttpHeaders.ETAG, expectedNextEtag))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.defendant_account_party.defendant_account_party_type").value("Defendant"));

        Map<String, Object> row = jdbcTemplate.queryForMap(
            "SELECT party_id, vehicle_make, vehicle_registration, "
                + "employer_name, employer_address_line_1, employee_reference, employer_telephone, "
                + "employer_email, document_language FROM debtor_detail WHERE party_id = ?",
            20010L);

        assertEquals(20010L, ((Number) row.get("party_id")).longValue());
        assertEquals("VW Golf", row.get("vehicle_make"));
        assertEquals("PU70ABC", row.get("vehicle_registration"));
        assertEquals("Put Employer", row.get("employer_name"));
        assertEquals("1 Employer Way", row.get("employer_address_line_1"));
        assertEquals("EN", row.get("document_language"));

        Integer updatedVersion = versionFor(20010L);
        assertEquals(currentVersion + 1, updatedVersion);
    }
}
