package uk.gov.hmcts.opal.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@Slf4j(topic = "opal.OpalDefendantsPutPartyIntegrationTest")
class OpalDefendantsPutPartyIntegrationTest extends AbstractOpalDefendantsIntegrationTest {

    @Test
    @DisplayName("OPAL: PUT Replace DAP – Not Found (account not in BU)")
    @JiraStory("PO-1963")
    @JiraEpic("PO-1970")
    @JiraTestKey("PO-6063")
    void put_notFound_whenAccountNotInHeaderBU() throws Exception {
        userStateStub.addPermissions((short) 99, FinesPermission.values());
        Integer currentVersion = versionFor(20010L);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken());
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
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
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
    @JiraStory("PO-1963")
    @JiraEpic("PO-1970")
    @JiraTestKey("PO-6067")
    void put_happyPath_updates_andReturnsResponse() throws Exception {

        Integer currentVersion = versionFor(20010L);
        String etag = "\"" + currentVersion + "\"";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userStateStub.getBearerToken());
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
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
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
    @JiraStory("PO-1963")
    @JiraEpic("PO-1970")
    @JiraTestKey("PO-6062")
    void put_notFound_whenDapMissing() throws Exception {
        Integer currentVersion = versionFor(20010L);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userStateStub.getBearerToken());
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
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
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
    @JiraStory("PO-1963")
    @JiraEpic("PO-1970")
    @JiraTestKey("PO-6066")
    void put_individual_aliases_upsert_and_trim() throws Exception {
        Integer currentVersion = versionFor(22004L);
        String etag = "\"" + currentVersion + "\"";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userStateStub.getBearerToken());
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
            put("/defendant-accounts/22004/defendant-account-parties/22004")
                .headers(headers)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
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
    @JiraStory("PO-1963")
    @JiraEpic("PO-1970")
    @JiraTestKey("PO-6068")
    void put_org_aliases_upsert_and_trim() throws Exception {
        Integer currentVersion = versionFor(20010L);
        assertNotNull(currentVersion);
        String etag = "\"" + currentVersion + "\"";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userStateStub.getBearerToken());
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
            put("/defendant-accounts/20010/defendant-account-parties/20010")
                .headers(headers)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
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
    @JiraStory("PO-1963")
    @JiraEpic("PO-1970")
    @JiraTestKey("PO-6065")
    void put_replace_dap_isDebtorFalse_clearsDebtorFieldsButKeepsRow() throws Exception {
        Integer currentVersion = versionFor(20010L);
        assertNotNull(currentVersion);
        String etag = "\"" + currentVersion + "\"";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userStateStub.getBearerToken());
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
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
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
    @JiraStory("PO-1963")
    @JiraEpic("PO-1970")
    @JiraTestKey("PO-6061")
    void put_replace_dap_isDebtorTrue_upsertsDebtorDetails() throws Exception {
        Integer currentVersion = versionFor(20010L);
        assertNotNull(currentVersion);
        String etag = "\"" + currentVersion + "\"";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userStateStub.getBearerToken());
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
            put("/defendant-accounts/20010/defendant-account-parties/20010")
                .headers(headers)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
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

    @Test
    @Sql(scripts = "classpath:db/insertData/update_into_parties.sql", executionPhase = BEFORE_TEST_METHOD)
    @DisplayName("OPAL: PUT Replace DAP – individual to organisation removes parent/guardian DAP in same tx")
    @JiraStory("PO-1963")
    @JiraEpic("PO-1970")
    @JiraTestKey("PO-6064")
    void put_convertIndividualToOrganisation_removesParentGuardianParty() throws Exception {
        Integer parentGuardianCountBefore = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM defendant_account_parties "
                + "WHERE defendant_account_id = 20010 AND association_type = 'Parent/Guardian'",
            Integer.class);
        assertEquals(1, parentGuardianCountBefore);

        Integer currentVersion = versionFor(20010L);
        String etag = "\"" + currentVersion + "\"";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userStateStub.getBearerToken());
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
                        "organisation_name": "Converted Co Ltd"
                    }
                }
            }
            """;

        ResultActions call = mockMvc.perform(
            put("/defendant-accounts/20010/defendant-account-parties/20010")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

        call.andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.ETAG, "\"" + (currentVersion + 1) + "\""))
            .andExpect(jsonPath("$.defendant_account_party.defendant_account_party_type").value("Defendant"));

        Integer parentGuardianCountAfter = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM defendant_account_parties "
                + "WHERE defendant_account_id = 20010 AND association_type = 'Parent/Guardian'",
            Integer.class);
        assertEquals(0, parentGuardianCountAfter);
    }


    @Nested
    @Sql(scripts = "classpath:db/insertData/insert_into_defendant_accounts_put_methods.sql", executionPhase = BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:db/deleteData/delete_from_defendant_accounts_put_methods.sql", executionPhase = AFTER_TEST_METHOD)
    //Suppressed LineLength as required for JSON comparison
    @SuppressWarnings("LineLength")
    class ReplaceDefendantAccountPartyTests {

        @Test
        @DisplayName("OPAL: PUT Replace DAP – Single name change creates one amendment.")
        @JiraStory("PO-2471")
        @JiraEpic("PO-1970")
        void put_singleNameChangeCreatesOneAmendment() throws Exception {
            authoriseAllPermissions();

            Integer currentVersion = versionFor(22005L);
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
                    "party_id": "22005",
                    "organisation_flag": false,
                    "individual_details": {
                      "title": "Mr",
                      "forenames": "Changed Forenames",
                      "surname": "SeedSurname22005",
                      "date_of_birth": "1990-01-01",
                      "national_insurance_number": "SNI22005",
                      "individual_aliases": [
                        { "alias_id": "2200501", "sequence_number": 1, "forenames": "AliasForenamesSeed", "surname": "AliasSurnameSeed" },
                        { "alias_id": "2200502", "sequence_number": 2, "forenames": "AliasForenamesSeed", "surname": "AliasSurnameSeed" }
                      ]
                    }
                  },
                  "address": {
                    "address_line_1": "Seed Address 22005",
                    "postcode": "SE2 0AA"
                  }
                }
                """;

            ResultActions call = mockMvc.perform(
                put("/defendant-accounts/22005/defendant-account-parties/22005").headers(headers)
                    .contentType(MediaType.APPLICATION_JSON).content(body));

            String expectedNextEtag = "\"" + (currentVersion + 1) + "\"";

            call.andExpect(status().isOk()).andExpect(header().string(HttpHeaders.ETAG, expectedNextEtag))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.defendant_account_party.defendant_account_party_type").value("Defendant"));

            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT * FROM amendments WHERE associated_record_id = '22005'");

            assertEquals(1, rows.size());
            assertEquals("Mr SeedForenames22005 SeedSurname22005", rows.get(0).get("old_value"));
            assertEquals("Mr Changed Forenames SeedSurname22005", rows.get(0).get("new_value"));

            List<Map<String, Object>> defendants = jdbcTemplate.queryForList(
                "SELECT last_changed_date FROM defendant_accounts WHERE defendant_account_id = '22005'");
            assertNotNull(defendants.getFirst().get("last_changed_date"));

            Integer updatedVersion = versionFor(22005L);
            assertEquals(currentVersion + 1, updatedVersion);
        }

        @Test
        @DisplayName("OPAL: PUT Replace DAP – Company name change creates one amendment.")
        @JiraStory("PO-2471")
        @JiraEpic("PO-1970")
        void put_companyNameChangeCreatesOneAmendment() throws Exception {
            authoriseAllPermissions();

            Integer currentVersion = versionFor(22006L);
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
                    "party_id": "22006",
                    "organisation_flag": true,
                    "organisation_details": {
                        "organisation_name": "Changed Org"
                    }
                  },
                  "address": {
                    "address_line_1": "Seed Address 22006",
                    "postcode": "SE2 0AA"
                  }
                }
                """;

            ResultActions call = mockMvc.perform(
                put("/defendant-accounts/22006/defendant-account-parties/22006").headers(headers)
                    .contentType(MediaType.APPLICATION_JSON).content(body));

            String expectedNextEtag = "\"" + (currentVersion + 1) + "\"";

            call.andExpect(status().isOk()).andExpect(header().string(HttpHeaders.ETAG, expectedNextEtag))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.defendant_account_party.defendant_account_party_type").value("Defendant"));

            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT * FROM amendments WHERE associated_record_id = '22006'");

            assertEquals(1, rows.size());
            assertEquals("Seed Org", rows.get(0).get("old_value"));
            assertEquals("Changed Org", rows.get(0).get("new_value"));

            List<Map<String, Object>> defendants = jdbcTemplate.queryForList(
                "SELECT last_changed_date FROM defendant_accounts WHERE defendant_account_id = '22006'");
            assertNotNull(defendants.getFirst().get("last_changed_date"));

            Integer updatedVersion = versionFor(22006L);
            assertEquals(currentVersion + 1, updatedVersion);
        }


        @Test
        @DisplayName("OPAL: PUT Replace DAP – Company name and address changes creates multiple amendments.")
        @JiraStory("PO-2471")
        @JiraEpic("PO-1970")
        void put_companyNameAddressChangeCreatesManyAmendments() throws Exception {
            authoriseAllPermissions();

            Integer currentVersion = versionFor(22006L);
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
                    "party_id": "22006",
                    "organisation_flag": true,
                    "organisation_details": {
                        "organisation_name": "Changed Org"
                    }
                  },
                  "address": {
                    "address_line_1": "Changed Address 22006",
                    "postcode": "SE3 0BB"
                  }
                }
                """;

            ResultActions call = mockMvc.perform(
                put("/defendant-accounts/22006/defendant-account-parties/22006").headers(headers)
                    .contentType(MediaType.APPLICATION_JSON).content(body));

            String expectedNextEtag = "\"" + (currentVersion + 1) + "\"";

            call.andExpect(status().isOk()).andExpect(header().string(HttpHeaders.ETAG, expectedNextEtag))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.defendant_account_party.defendant_account_party_type").value("Defendant"));

            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT * FROM amendments WHERE associated_record_id = '22006'");

            assertEquals(3, rows.size());
            assertEquals("Seed Org", rows.get(0).get("old_value"));
            assertEquals("Changed Org", rows.get(0).get("new_value"));
            assertEquals("Seed Address 22006", rows.get(1).get("old_value"));
            assertEquals("Changed Address 22006", rows.get(1).get("new_value"));
            assertEquals("SE2 0AA", rows.get(2).get("old_value"));
            assertEquals("SE3 0BB", rows.get(2).get("new_value"));


            List<Map<String, Object>> defendants = jdbcTemplate.queryForList(
                "SELECT last_changed_date FROM defendant_accounts WHERE defendant_account_id = '22006'");
            assertNotNull(defendants.getFirst().get("last_changed_date"));

            Integer updatedVersion = versionFor(22006L);
            assertEquals(currentVersion + 1, updatedVersion);
        }

        @Test
        @DisplayName("OPAL: PUT Replace DAP – Multiple alias changes creates multiple amendments.")
        @JiraStory("PO-2471")
        @JiraEpic("PO-1970")
        void put_manyAliasChangesCreatesManyAmendments() throws Exception {
            authoriseAllPermissions();

            Integer currentVersion = versionFor(22005L);
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
                    "party_id": "22005",
                    "organisation_flag": false,
                    "individual_details": {
                      "title": "Mr",
                      "forenames": "SeedForenames22005",
                      "surname": "SeedSurname22005",
                      "date_of_birth": "1990-01-01",
                      "national_insurance_number": "SNI22005",
                      "individual_aliases": [
                        { "alias_id": "2200501", "sequence_number": 1, "forenames": "Changed Forename", "surname": "AliasSurnameSeed" },
                        { "alias_id": "2200502", "sequence_number": 2, "forenames": "Changed Forename", "surname": "AliasSurnameSeed" }
                      ]
                    }
                  },
                  "address": {
                    "address_line_1": "Seed Address 22005",
                    "postcode": "SE2 0AA"
                  }
                }
                """;

            ResultActions call = mockMvc.perform(
                put("/defendant-accounts/22005/defendant-account-parties/22005").headers(headers)
                    .contentType(MediaType.APPLICATION_JSON).content(body));

            String expectedNextEtag = "\"" + (currentVersion + 1) + "\"";

            call.andExpect(status().isOk()).andExpect(header().string(HttpHeaders.ETAG, expectedNextEtag))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.defendant_account_party.defendant_account_party_type").value("Defendant"));

            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT * FROM amendments WHERE associated_record_id = '22005'");

            assertEquals(2, rows.size());
            assertEquals("AliasForenamesSeed AliasSurnameSeed", rows.get(0).get("old_value"));
            assertEquals("Changed Forename AliasSurnameSeed", rows.get(0).get("new_value"));
            assertEquals("AliasForenamesSeed AliasSurnameSeed", rows.get(1).get("old_value"));
            assertEquals("Changed Forename AliasSurnameSeed", rows.get(1).get("new_value"));

            List<Map<String, Object>> defendants = jdbcTemplate.queryForList(
                "SELECT last_changed_date FROM defendant_accounts WHERE defendant_account_id = '22005'");
            assertNotNull(defendants.getFirst().get("last_changed_date"));

            Integer updatedVersion = versionFor(22005L);
            assertEquals(currentVersion + 1, updatedVersion);
        }

        @Test
        @DisplayName("OPAL: PUT Replace DAP – multiple address changes creates multiple amendments.")
        @JiraStory("PO-2471")
        @JiraEpic("PO-1970")
        void put_manyAddressChangesCreatesManyAmendments() throws Exception {
            authoriseAllPermissions();

            Integer currentVersion = versionFor(22005L);
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
                    "party_id": "22005",
                    "organisation_flag": false,
                    "individual_details": {
                      "title": "Mr",
                      "forenames": "SeedForenames22005",
                      "surname": "SeedSurname22005",
                      "date_of_birth": "1990-01-01",
                      "national_insurance_number": "SNI22005",
                      "individual_aliases": [
                        { "alias_id": "2200501", "sequence_number": 1, "forenames": "AliasForenamesSeed", "surname": "AliasSurnameSeed" },
                        { "alias_id": "2200502", "sequence_number": 2, "forenames": "AliasForenamesSeed", "surname": "AliasSurnameSeed" }
                      ]
                    }
                  },
                  "address": {
                    "address_line_1": "Changed Address 22005",
                    "postcode": "SE3 0BB"
                  }
                }
                """;

            ResultActions call = mockMvc.perform(
                put("/defendant-accounts/22005/defendant-account-parties/22005").headers(headers)
                    .contentType(MediaType.APPLICATION_JSON).content(body));

            String expectedNextEtag = "\"" + (currentVersion + 1) + "\"";

            call.andExpect(status().isOk()).andExpect(header().string(HttpHeaders.ETAG, expectedNextEtag))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.defendant_account_party.defendant_account_party_type").value("Defendant"));

            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT * FROM amendments WHERE associated_record_id = '22005'");

            assertEquals(2, rows.size());
            assertEquals("Seed Address 22005", rows.get(0).get("old_value"));
            assertEquals("Changed Address 22005", rows.get(0).get("new_value"));
            assertEquals("SE2 0AA", rows.get(1).get("old_value"));
            assertEquals("SE3 0BB", rows.get(1).get("new_value"));


            List<Map<String, Object>> defendants = jdbcTemplate.queryForList(
                "SELECT last_changed_date FROM defendant_accounts WHERE defendant_account_id = '22005'");
            assertNotNull(defendants.getFirst().get("last_changed_date"));

            Integer updatedVersion = versionFor(22005L);
            assertEquals(currentVersion + 1, updatedVersion);
        }

        @Test
        @DisplayName("OPAL: PUT Replace DAP – Postcode changes creates single amendment.")
        @JiraStory("PO-2471")
        @JiraEpic("PO-1970")
        void put_postcodeChangeCreatesSingleAmendment() throws Exception {
            authoriseAllPermissions();

            Integer currentVersion = versionFor(22005L);
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
                    "party_id": "22005",
                    "organisation_flag": false,
                    "individual_details": {
                      "title": "Mr",
                      "forenames": "SeedForenames22005",
                      "surname": "SeedSurname22005",
                      "date_of_birth": "1990-01-01",
                      "national_insurance_number": "SNI22005",
                      "individual_aliases": [
                        { "alias_id": "2200501", "sequence_number": 1, "forenames": "AliasForenamesSeed", "surname": "AliasSurnameSeed" },
                        { "alias_id": "2200502", "sequence_number": 2, "forenames": "AliasForenamesSeed", "surname": "AliasSurnameSeed" }
                      ]
                    }
                  },
                  "address": {
                    "address_line_1": "Seed Address 22005",
                    "postcode": "NEW PC0"
                  }
                }
                """;

            ResultActions call = mockMvc.perform(
                put("/defendant-accounts/22005/defendant-account-parties/22005").headers(headers)
                    .contentType(MediaType.APPLICATION_JSON).content(body));

            String expectedNextEtag = "\"" + (currentVersion + 1) + "\"";

            call.andExpect(status().isOk()).andExpect(header().string(HttpHeaders.ETAG, expectedNextEtag))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.defendant_account_party.defendant_account_party_type").value("Defendant"));

            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT * FROM amendments WHERE associated_record_id = '22005'");

            assertEquals(1, rows.size());
            assertEquals("SE2 0AA", rows.get(0).get("old_value"));
            assertEquals("NEW PC0", rows.get(0).get("new_value"));

            List<Map<String, Object>> defendants = jdbcTemplate.queryForList(
                "SELECT last_changed_date FROM defendant_accounts WHERE defendant_account_id = '22005'");
            assertNotNull(defendants.getFirst().get("last_changed_date"));

            Integer updatedVersion = versionFor(22005L);
            assertEquals(currentVersion + 1, updatedVersion);
        }

        @Test
        @DisplayName("OPAL: PUT Replace DAP – Date of birth change creates single amendment.")
        @JiraStory("PO-2471")
        @JiraEpic("PO-1970")
        void put_dobChangeCreatesSingleAmendment() throws Exception {
            authoriseAllPermissions();

            Integer currentVersion = versionFor(22005L);
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
                    "party_id": "22005",
                    "organisation_flag": false,
                    "individual_details": {
                      "title": "Mr",
                      "forenames": "SeedForenames22005",
                      "surname": "SeedSurname22005",
                      "date_of_birth": "2000-01-01",
                      "national_insurance_number": "SNI22005",
                      "individual_aliases": [
                        { "alias_id": "2200501", "sequence_number": 1, "forenames": "AliasForenamesSeed", "surname": "AliasSurnameSeed" },
                        { "alias_id": "2200502", "sequence_number": 2, "forenames": "AliasForenamesSeed", "surname": "AliasSurnameSeed" }
                      ]
                    }
                  },
                  "address": {
                    "address_line_1": "Seed Address 22005",
                    "postcode": "SE2 0AA"
                  }
                }
                """;

            ResultActions call = mockMvc.perform(
                put("/defendant-accounts/22005/defendant-account-parties/22005").headers(headers)
                    .contentType(MediaType.APPLICATION_JSON).content(body));

            String expectedNextEtag = "\"" + (currentVersion + 1) + "\"";

            call.andExpect(status().isOk()).andExpect(header().string(HttpHeaders.ETAG, expectedNextEtag))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.defendant_account_party.defendant_account_party_type").value("Defendant"));

            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT * FROM amendments WHERE associated_record_id = '22005'");

            assertEquals(1, rows.size());
            assertEquals("1990-01-01 00:00:00", rows.get(0).get("old_value"));
            assertEquals("2000-01-01 00:00:00", rows.get(0).get("new_value"));

            List<Map<String, Object>> defendants = jdbcTemplate.queryForList(
                "SELECT last_changed_date FROM defendant_accounts WHERE defendant_account_id = '22005'");
            assertNotNull(defendants.getFirst().get("last_changed_date"));

            Integer updatedVersion = versionFor(22005L);
            assertEquals(currentVersion + 1, updatedVersion);
        }

        @Test
        @DisplayName("OPAL: PUT Replace DAP – Name and date of birth change creates two amendments.")
        @JiraStory("PO-2471")
        @JiraEpic("PO-1970")
        void put_nameAndDOBChangeCreatesTwoAmendments() throws Exception {
            authoriseAllPermissions();

            Integer currentVersion = versionFor(22005L);
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
                    "party_id": "22005",
                    "organisation_flag": false,
                    "individual_details": {
                      "title": "Mr",
                      "forenames": "Changed Name",
                      "surname": "SeedSurname22005",
                      "date_of_birth": "2000-01-01",
                      "national_insurance_number": "SNI22005",
                      "individual_aliases": [
                        { "alias_id": "2200501", "sequence_number": 1, "forenames": "AliasForenamesSeed", "surname": "AliasSurnameSeed" },
                        { "alias_id": "2200502", "sequence_number": 2, "forenames": "AliasForenamesSeed", "surname": "AliasSurnameSeed" }
                      ]
                    }
                  },
                  "address": {
                    "address_line_1": "Seed Address 22005",
                    "postcode": "SE2 0AA"
                  }
                }
                """;

            ResultActions call = mockMvc.perform(
                put("/defendant-accounts/22005/defendant-account-parties/22005").headers(headers)
                    .contentType(MediaType.APPLICATION_JSON).content(body));

            String expectedNextEtag = "\"" + (currentVersion + 1) + "\"";

            call.andExpect(status().isOk()).andExpect(header().string(HttpHeaders.ETAG, expectedNextEtag))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.defendant_account_party.defendant_account_party_type").value("Defendant"));

            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT * FROM amendments WHERE associated_record_id = '22005'");

            assertEquals(2, rows.size());
            assertEquals("Mr SeedForenames22005 SeedSurname22005", rows.get(0).get("old_value"));
            assertEquals("Mr Changed Name SeedSurname22005", rows.get(0).get("new_value"));
            assertEquals("1990-01-01 00:00:00", rows.get(1).get("old_value"));
            assertEquals("2000-01-01 00:00:00", rows.get(1).get("new_value"));

            List<Map<String, Object>> defendants = jdbcTemplate.queryForList(
                "SELECT last_changed_date FROM defendant_accounts WHERE defendant_account_id = '22005'");
            assertNotNull(defendants.getFirst().get("last_changed_date"));

            Integer updatedVersion = versionFor(22005L);
            assertEquals(currentVersion + 1, updatedVersion);
        }

        @Test
        @DisplayName("OPAL: PUT Replace DAP – Name plus alias changes creates multiple amendments.")
        @JiraStory("PO-2471")
        @JiraEpic("PO-1970")
        void put_nameAndAliasChangeCreatesManyAmendments() throws Exception {
            authoriseAllPermissions();

            Integer currentVersion = versionFor(22005L);
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
                    "party_id": "22005",
                    "organisation_flag": false,
                    "individual_details": {
                      "title": "Mr",
                      "forenames": "Changed Forename",
                      "surname": "SeedSurname22005",
                      "date_of_birth": "1990-01-01",
                      "national_insurance_number": "SNI22005",
                      "individual_aliases": [
                        { "alias_id": "2200501", "sequence_number": 1, "forenames": "Changed ForenamesAlias", "surname": "AliasSurnameSeed" },
                        { "alias_id": "2200502", "sequence_number": 2, "forenames": "AliasForenamesSeed", "surname": "Changed SurnameAlias" }
                      ]
                    }
                  },
                  "address": {
                    "address_line_1": "Seed Address 22005",
                    "postcode": "SE2 0AA"
                  }
                }
                """;

            ResultActions call = mockMvc.perform(
                put("/defendant-accounts/22005/defendant-account-parties/22005").headers(headers)
                    .contentType(MediaType.APPLICATION_JSON).content(body));

            String expectedNextEtag = "\"" + (currentVersion + 1) + "\"";

            call.andExpect(status().isOk()).andExpect(header().string(HttpHeaders.ETAG, expectedNextEtag))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.defendant_account_party.defendant_account_party_type").value("Defendant"));

            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT * FROM amendments WHERE associated_record_id = '22005'");

            assertEquals(3, rows.size());
            assertEquals("Mr SeedForenames22005 SeedSurname22005", rows.get(0).get("old_value"));
            assertEquals("Mr Changed Forename SeedSurname22005", rows.get(0).get("new_value"));
            assertEquals("AliasForenamesSeed AliasSurnameSeed", rows.get(1).get("old_value"));
            assertEquals("Changed ForenamesAlias AliasSurnameSeed", rows.get(1).get("new_value"));
            assertEquals("AliasForenamesSeed AliasSurnameSeed", rows.get(2).get("old_value"));
            assertEquals("AliasForenamesSeed Changed SurnameAlias", rows.get(2).get("new_value"));

            List<Map<String, Object>> defendants = jdbcTemplate.queryForList(
                "SELECT last_changed_date FROM defendant_accounts WHERE defendant_account_id = '22005'");
            assertNotNull(defendants.getFirst().get("last_changed_date"));

            Integer updatedVersion = versionFor(22005L);
            assertEquals(currentVersion + 1, updatedVersion);
        }

        @Test
        @DisplayName("OPAL: PUT Replace DAP – Name and address changes creates multiple amendments.")
        @JiraStory("PO-2471")
        @JiraEpic("PO-1970")
        void put_nameAddressChangeCreatesManyAmendments() throws Exception {
            authoriseAllPermissions();

            Integer currentVersion = versionFor(22005L);
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
                    "party_id": "22005",
                    "organisation_flag": false,
                    "individual_details": {
                      "title": "Mr",
                      "forenames": "Changed Forenames",
                      "surname": "SeedSurname22005",
                      "date_of_birth": "1990-01-01",
                      "national_insurance_number": "SNI22005",
                      "individual_aliases": [
                        { "alias_id": "2200501", "sequence_number": 1, "forenames": "AliasForenamesSeed", "surname": "AliasSurnameSeed" },
                        { "alias_id": "2200502", "sequence_number": 2, "forenames": "AliasForenamesSeed", "surname": "AliasSurnameSeed" }
                      ]
                    }
                  },
                  "address": {
                    "address_line_1": "Changed Address 22005",
                    "postcode": "NEW PC0"
                  }
                }
                """;

            ResultActions call = mockMvc.perform(
                put("/defendant-accounts/22005/defendant-account-parties/22005").headers(headers)
                    .contentType(MediaType.APPLICATION_JSON).content(body));

            String expectedNextEtag = "\"" + (currentVersion + 1) + "\"";

            call.andExpect(status().isOk()).andExpect(header().string(HttpHeaders.ETAG, expectedNextEtag))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.defendant_account_party.defendant_account_party_type").value("Defendant"));

            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT * FROM amendments WHERE associated_record_id = '22005'");

            assertEquals(3, rows.size());
            assertEquals("Mr SeedForenames22005 SeedSurname22005", rows.get(0).get("old_value"));
            assertEquals("Mr Changed Forenames SeedSurname22005", rows.get(0).get("new_value"));
            assertEquals("Seed Address 22005", rows.get(1).get("old_value"));
            assertEquals("Changed Address 22005", rows.get(1).get("new_value"));
            assertEquals("SE2 0AA", rows.get(2).get("old_value"));
            assertEquals("NEW PC0", rows.get(2).get("new_value"));

            List<Map<String, Object>> defendants = jdbcTemplate.queryForList(
                "SELECT last_changed_date FROM defendant_accounts WHERE defendant_account_id = '22005'");
            assertNotNull(defendants.getFirst().get("last_changed_date"));

            Integer updatedVersion = versionFor(22005L);
            assertEquals(currentVersion + 1, updatedVersion);
        }

        @Test
        @DisplayName("OPAL: PUT Replace DAP – No field changes creates no amendments")
        @JiraStory("PO-2471")
        @JiraEpic("PO-1970")
        void put_noChangeCreatesNoAmendment() throws Exception {
            authoriseAllPermissions();

            Integer currentVersion = versionFor(22005L);
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
                    "party_id": "22005",
                    "organisation_flag": false,
                    "individual_details": {
                      "title": "Mr",
                      "forenames": "SeedForenames22005",
                      "surname": "SeedSurname22005",
                      "date_of_birth": "1990-01-01",
                      "national_insurance_number": "SNI22005",
                      "individual_aliases": [
                        { "alias_id": "2200501", "sequence_number": 1, "forenames": "AliasForenamesSeed", "surname": "AliasSurnameSeed" },
                        { "alias_id": "2200502", "sequence_number": 2, "forenames": "AliasForenamesSeed", "surname": "AliasSurnameSeed" }
                      ]
                    }
                  },
                  "address": {
                    "address_line_1": "Seed Address 22005",
                    "postcode": "SE2 0AA"
                  }
                }
                """;

            ResultActions call = mockMvc.perform(
                put("/defendant-accounts/22005/defendant-account-parties/22005").headers(headers)
                    .contentType(MediaType.APPLICATION_JSON).content(body));
            String expectedNextEtag = "\"" + (currentVersion + 1) + "\"";

            call.andExpect(status().isOk()).andExpect(header().string(HttpHeaders.ETAG, expectedNextEtag))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.defendant_account_party.defendant_account_party_type").value("Defendant"));

            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT * FROM amendments WHERE associated_record_id = '22005'");

            assertEquals(0, rows.size());

            Integer updatedVersion = versionFor(22005L);
            assertEquals(currentVersion + 1, updatedVersion);
        }
    }
}
