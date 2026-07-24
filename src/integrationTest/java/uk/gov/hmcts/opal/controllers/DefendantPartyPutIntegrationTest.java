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
import uk.gov.hmcts.opal.entity.AliasEntity;
import uk.gov.hmcts.opal.entity.amendment.AmendmentEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.AssociationType;
import uk.gov.hmcts.opal.entity.debtordetail.Language;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@Slf4j(topic = "opal.DefendantPartyPutIntegrationTest")
class DefendantPartyPutIntegrationTest extends AbstractOpalDefendantsIntegrationTest {

    @Test
    @Sql(
        scripts = "classpath:db/insertData/update_into_parties.sql",
        executionPhase = BEFORE_TEST_METHOD
    )
    @DisplayName("OPAL: PUT Replace DAP - account controls return 422 for P/G replacement")
    @JiraStory("PO-5757")
    @JiraEpic("PO-2990")
    void put_replaceParentGuardian_returns422_whenBlockedByAccountControls() throws Exception {
        long defendantAccountId = 20010L;
        Integer currentVersion = versionFor(defendantAccountId);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userStateStub.getBearerToken());
        headers.add("Business-Unit-Id", "78");
        headers.add(HttpHeaders.IF_MATCH, "\"" + currentVersion + "\"");

        String body = """
            {
                "defendant_account_party_type": "Parent/Guardian",
                "is_debtor": true,
                "party_details": {
                    "party_id": "920010",
                    "organisation_flag": false,
                    "individual_details": {
                        "title": "Mr",
                        "forenames": "Blocked",
                        "surname": "Guardian"
                    }
                }
            }
            """;

        ResultActions res = mockMvc.perform(
            put("/defendant-accounts/20010/defendant-account-parties/920011")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        );

        log.info("PUT DAP account controls resp:\n{}", res.andReturn().getResponse().getContentAsString());

        res.andExpect(status().isUnprocessableEntity())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Unprocessable Content"))
            .andExpect(jsonPath("$.status").value(422))
            .andExpect(jsonPath("$.detail").value(
                "Defendant account update blocked: Account Status Check failed because account_status is CS."))
            .andExpect(jsonPath("$.retriable").value(false));

        assertEquals(currentVersion, versionFor(defendantAccountId));
    }

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

        List<AliasEntity> aliases = aliasesForParty(22004L);

        assertEquals(2, aliases.size());
        assertEquals(1, aliases.get(0).getSequenceNumber());
        assertEquals(2200401L, aliases.get(0).getAliasId());
        assertEquals("Jane", aliases.get(0).getForenames());
        assertEquals("Doe", aliases.get(0).getSurname());
        assertNull(aliases.get(0).getOrganisationName());

        assertEquals(2, aliases.get(1).getSequenceNumber());
        assertNotNull(aliases.get(1).getAliasId());
        assertNotEquals(2200401L, aliases.get(1).getAliasId());
        assertEquals("J.", aliases.get(1).getForenames());
        assertEquals("Smith", aliases.get(1).getSurname());
        assertNull(aliases.get(1).getOrganisationName());

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

        List<AliasEntity> aliases = aliasesForParty(20010L);

        assertEquals(2, aliases.size(), "Expected exactly 2 aliases after upsert+trim");

        AliasEntity firstAlias = aliases.get(0);
        assertEquals(2, firstAlias.getSequenceNumber());
        assertEquals(200101L, firstAlias.getAliasId());
        assertEquals("PutCo Alias One (updated)", firstAlias.getOrganisationName());
        assertNull(firstAlias.getSurname());
        assertNull(firstAlias.getForenames());

        AliasEntity secondAlias = aliases.get(1);
        assertEquals(3, secondAlias.getSequenceNumber());
        Long newId = secondAlias.getAliasId();
        assertNotNull(newId);
        assertNotEquals(200102L, newId, "Omitted alias 200102 must have been deleted");
        assertEquals("PutCo Alias Three (new)", secondAlias.getOrganisationName());
        assertNull(secondAlias.getSurname());
        assertNull(secondAlias.getForenames());

        assertEquals(0, aliasCountFor(20010L, 200102L), "Alias 200102 should be trimmed");

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

        var debtorDetail = debtorDetailFor(20010L);

        assertEquals(20010L, debtorDetail.getPartyId());
        assertNull(debtorDetail.getVehicleMake(), "vehicle_make should be cleared to null");
        assertNull(debtorDetail.getVehicleRegistration(), "vehicle_registration should be cleared to null");
        assertNull(debtorDetail.getEmployerName(), "employer_name should be cleared to null");
        assertNull(debtorDetail.getDocumentLanguage(), "document_language should be cleared to null");

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

        var debtorDetail = debtorDetailFor(20010L);

        assertEquals(20010L, debtorDetail.getPartyId());
        assertEquals("VW Golf", debtorDetail.getVehicleMake());
        assertEquals("PU70ABC", debtorDetail.getVehicleRegistration());
        assertEquals("Put Employer", debtorDetail.getEmployerName());
        assertEquals("1 Employer Way", debtorDetail.getEmployerAddressLine1());
        assertEquals(Language.ENGLISH, debtorDetail.getDocumentLanguage());

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
        int parentGuardianCountBefore = partyAssociationCountFor(20010L, AssociationType.PARENT_GUARDIAN);
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

        int parentGuardianCountAfter = partyAssociationCountFor(20010L, AssociationType.PARENT_GUARDIAN);
        assertEquals(0, parentGuardianCountAfter);
    }


    @Nested
    @Sql(scripts = "classpath:db/insertData/insert_into_defendant_accounts_put_methods.sql", executionPhase = BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:db/deleteData/delete_from_defendant_accounts_put_methods.sql", executionPhase = AFTER_TEST_METHOD)
    //Suppressed LineLength as required for JSON comparison
    @SuppressWarnings("LineLength")
    class ReplaceDefendantAccountPartyTests {

        private static final String EXPECTED_AMENDED_BY = "L078JG";
        private static final String EXPECTED_AMENDED_BY_NAME = "opal-test@HMCTS.NET";

        @Test
        @DisplayName("OPAL: PUT Replace DAP – Single name change creates one amendment.")
        @JiraStory("PO-2471")
        @JiraEpic("PO-1970")
        @JiraTestKey("PO-8770")
        void put_singleNameChangeCreatesOneAmendment() throws Exception {
            userStateStub.addPermissions((short) 78, FinesPermission.values());

            Integer currentVersion = versionFor(22005L);
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
                    .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                    .contentType(MediaType.APPLICATION_JSON).content(body));

            String expectedNextEtag = "\"" + (currentVersion + 1) + "\"";

            call.andExpect(status().isOk()).andExpect(header().string(HttpHeaders.ETAG, expectedNextEtag))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.defendant_account_party.defendant_account_party_type").value("Defendant"));

            List<AmendmentEntity> amendments = defendantAccountAmendmentsFor(22005L);

            assertEquals(1, amendments.size());
            assertAmendment(amendments.getFirst(), "Mr SeedForenames22005 SeedSurname22005",
                "Mr Changed Forenames SeedSurname22005");
            assertAuditIdentity(amendments);

            assertNotNull(lastChangedDateFor(22005L));

            Integer updatedVersion = versionFor(22005L);
            assertEquals(currentVersion + 1, updatedVersion);
        }

        @Test
        @DisplayName("OPAL: PUT Replace DAP – Company name change creates one amendment.")
        @JiraStory("PO-2471")
        @JiraEpic("PO-1970")
        @JiraTestKey("PO-8773")
        void put_companyNameChangeCreatesOneAmendment() throws Exception {
            userStateStub.addPermissions((short) 78, FinesPermission.values());

            Integer currentVersion = versionFor(22006L);
            String etag = "\"" + currentVersion + "\"";

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken());
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
                    .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                    .contentType(MediaType.APPLICATION_JSON).content(body));

            String expectedNextEtag = "\"" + (currentVersion + 1) + "\"";

            call.andExpect(status().isOk()).andExpect(header().string(HttpHeaders.ETAG, expectedNextEtag))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.defendant_account_party.defendant_account_party_type").value("Defendant"));

            List<AmendmentEntity> amendments = defendantAccountAmendmentsFor(22006L);

            assertEquals(1, amendments.size());
            assertAmendment(amendments.getFirst(), "Seed Org", "Changed Org");

            assertNotNull(lastChangedDateFor(22006L));

            Integer updatedVersion = versionFor(22006L);
            assertEquals(currentVersion + 1, updatedVersion);
        }


        @Test
        @DisplayName("OPAL: PUT Replace DAP – Company name and address changes creates multiple amendments.")
        @JiraStory("PO-2471")
        @JiraEpic("PO-1970")
        @JiraTestKey("PO-8774")
        void put_companyNameAddressChangeCreatesManyAmendments() throws Exception {
            userStateStub.addPermissions((short) 78, FinesPermission.values());

            Integer currentVersion = versionFor(22006L);
            String etag = "\"" + currentVersion + "\"";

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken());
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
                    .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                    .contentType(MediaType.APPLICATION_JSON).content(body));

            String expectedNextEtag = "\"" + (currentVersion + 1) + "\"";

            call.andExpect(status().isOk()).andExpect(header().string(HttpHeaders.ETAG, expectedNextEtag))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.defendant_account_party.defendant_account_party_type").value("Defendant"));

            List<AmendmentEntity> amendments = defendantAccountAmendmentsFor(22006L);

            assertEquals(3, amendments.size());
            assertAmendment(amendments.get(0), "Seed Org", "Changed Org");
            assertAmendment(amendments.get(1), "Seed Address 22006", "Changed Address 22006");
            assertAmendment(amendments.get(2), "SE2 0AA", "SE3 0BB");
            assertAuditIdentity(amendments);


            assertNotNull(lastChangedDateFor(22006L));

            Integer updatedVersion = versionFor(22006L);
            assertEquals(currentVersion + 1, updatedVersion);
        }

        private void assertAuditIdentity(List<AmendmentEntity> amendments) {
            for (AmendmentEntity amendment : amendments) {
                assertEquals(EXPECTED_AMENDED_BY, amendment.getAmendedBy());
                assertEquals(EXPECTED_AMENDED_BY_NAME, amendment.getAmendedByName());
            }
        }

        private void assertAmendment(AmendmentEntity amendment, String expectedOldValue, String expectedNewValue) {
            assertEquals(expectedOldValue, amendment.getOldValue());
            assertEquals(expectedNewValue, amendment.getNewValue());
        }

        @Test
        @DisplayName("OPAL: PUT Replace DAP – Multiple alias changes creates multiple amendments.")
        @JiraStory("PO-2471")
        @JiraEpic("PO-1970")
        @JiraTestKey("PO-8775")
        void put_manyAliasChangesCreatesManyAmendments() throws Exception {
            userStateStub.addPermissions((short) 78, FinesPermission.values());

            Integer currentVersion = versionFor(22005L);
            String etag = "\"" + currentVersion + "\"";

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken());
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
                    .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                    .contentType(MediaType.APPLICATION_JSON).content(body));

            String expectedNextEtag = "\"" + (currentVersion + 1) + "\"";

            call.andExpect(status().isOk()).andExpect(header().string(HttpHeaders.ETAG, expectedNextEtag))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.defendant_account_party.defendant_account_party_type").value("Defendant"));

            List<AmendmentEntity> amendments = defendantAccountAmendmentsFor(22005L);

            assertEquals(2, amendments.size());
            assertAmendment(amendments.get(0), "AliasForenamesSeed AliasSurnameSeed",
                "Changed Forename AliasSurnameSeed");
            assertAmendment(amendments.get(1), "AliasForenamesSeed AliasSurnameSeed",
                "Changed Forename AliasSurnameSeed");

            assertNotNull(lastChangedDateFor(22005L));

            Integer updatedVersion = versionFor(22005L);
            assertEquals(currentVersion + 1, updatedVersion);
        }

        @Test
        @DisplayName("OPAL: PUT Replace DAP – multiple address changes creates multiple amendments.")
        @JiraStory("PO-2471")
        @JiraEpic("PO-1970")
        @JiraTestKey("PO-8772")
        void put_manyAddressChangesCreatesManyAmendments() throws Exception {
            userStateStub.addPermissions((short) 78, FinesPermission.values());

            Integer currentVersion = versionFor(22005L);
            String etag = "\"" + currentVersion + "\"";

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken());
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
                    .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                    .contentType(MediaType.APPLICATION_JSON).content(body));

            String expectedNextEtag = "\"" + (currentVersion + 1) + "\"";

            call.andExpect(status().isOk()).andExpect(header().string(HttpHeaders.ETAG, expectedNextEtag))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.defendant_account_party.defendant_account_party_type").value("Defendant"));

            List<AmendmentEntity> amendments = defendantAccountAmendmentsFor(22005L);

            assertEquals(2, amendments.size());
            assertAmendment(amendments.get(0), "Seed Address 22005", "Changed Address 22005");
            assertAmendment(amendments.get(1), "SE2 0AA", "SE3 0BB");


            assertNotNull(lastChangedDateFor(22005L));

            Integer updatedVersion = versionFor(22005L);
            assertEquals(currentVersion + 1, updatedVersion);
        }

        @Test
        @DisplayName("OPAL: PUT Replace DAP – Postcode changes creates single amendment.")
        @JiraStory("PO-2471")
        @JiraEpic("PO-1970")
        @JiraTestKey("PO-8776")
        void put_postcodeChangeCreatesSingleAmendment() throws Exception {
            userStateStub.addPermissions((short) 78, FinesPermission.values());

            Integer currentVersion = versionFor(22005L);
            String etag = "\"" + currentVersion + "\"";

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken());
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
                    .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                    .contentType(MediaType.APPLICATION_JSON).content(body));

            String expectedNextEtag = "\"" + (currentVersion + 1) + "\"";

            call.andExpect(status().isOk()).andExpect(header().string(HttpHeaders.ETAG, expectedNextEtag))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.defendant_account_party.defendant_account_party_type").value("Defendant"));

            List<AmendmentEntity> amendments = defendantAccountAmendmentsFor(22005L);

            assertEquals(1, amendments.size());
            assertAmendment(amendments.getFirst(), "SE2 0AA", "NEW PC0");

            assertNotNull(lastChangedDateFor(22005L));

            Integer updatedVersion = versionFor(22005L);
            assertEquals(currentVersion + 1, updatedVersion);
        }

        @Test
        @DisplayName("OPAL: PUT Replace DAP – Date of birth change creates single amendment.")
        @JiraStory("PO-2471")
        @JiraEpic("PO-1970")
        @JiraTestKey("PO-8767")
        void put_dobChangeCreatesSingleAmendment() throws Exception {
            userStateStub.addPermissions((short) 78, FinesPermission.values());

            Integer currentVersion = versionFor(22005L);
            String etag = "\"" + currentVersion + "\"";

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken());
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
                    .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                    .contentType(MediaType.APPLICATION_JSON).content(body));

            String expectedNextEtag = "\"" + (currentVersion + 1) + "\"";

            call.andExpect(status().isOk()).andExpect(header().string(HttpHeaders.ETAG, expectedNextEtag))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.defendant_account_party.defendant_account_party_type").value("Defendant"));

            List<AmendmentEntity> amendments = defendantAccountAmendmentsFor(22005L);

            assertEquals(1, amendments.size());
            assertAmendment(amendments.getFirst(), "1990-01-01 00:00:00", "2000-01-01 00:00:00");

            assertNotNull(lastChangedDateFor(22005L));

            Integer updatedVersion = versionFor(22005L);
            assertEquals(currentVersion + 1, updatedVersion);
        }

        @Test
        @DisplayName("OPAL: PUT Replace DAP – Name and date of birth change creates two amendments.")
        @JiraStory("PO-2471")
        @JiraEpic("PO-1970")
        @JiraTestKey("PO-8771")
        void put_nameAndDOBChangeCreatesTwoAmendments() throws Exception {
            userStateStub.addPermissions((short) 78, FinesPermission.values());

            Integer currentVersion = versionFor(22005L);
            String etag = "\"" + currentVersion + "\"";

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken());
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
                    .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                    .contentType(MediaType.APPLICATION_JSON).content(body));

            String expectedNextEtag = "\"" + (currentVersion + 1) + "\"";

            call.andExpect(status().isOk()).andExpect(header().string(HttpHeaders.ETAG, expectedNextEtag))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.defendant_account_party.defendant_account_party_type").value("Defendant"));

            List<AmendmentEntity> amendments = defendantAccountAmendmentsFor(22005L);

            assertEquals(2, amendments.size());
            assertAmendment(amendments.get(0), "Mr SeedForenames22005 SeedSurname22005",
                "Mr Changed Name SeedSurname22005");
            assertAmendment(amendments.get(1), "1990-01-01 00:00:00", "2000-01-01 00:00:00");

            assertNotNull(lastChangedDateFor(22005L));

            Integer updatedVersion = versionFor(22005L);
            assertEquals(currentVersion + 1, updatedVersion);
        }

        @Test
        @DisplayName("OPAL: PUT Replace DAP – Name plus alias changes creates multiple amendments.")
        @JiraStory("PO-2471")
        @JiraEpic("PO-1970")
        @JiraTestKey("PO-8769")
        void put_nameAndAliasChangeCreatesManyAmendments() throws Exception {
            userStateStub.addPermissions((short) 78, FinesPermission.values());

            Integer currentVersion = versionFor(22005L);
            String etag = "\"" + currentVersion + "\"";

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken());
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
                    .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                    .contentType(MediaType.APPLICATION_JSON).content(body));

            String expectedNextEtag = "\"" + (currentVersion + 1) + "\"";

            call.andExpect(status().isOk()).andExpect(header().string(HttpHeaders.ETAG, expectedNextEtag))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.defendant_account_party.defendant_account_party_type").value("Defendant"));

            List<AmendmentEntity> amendments = defendantAccountAmendmentsFor(22005L);

            assertEquals(3, amendments.size());
            assertAmendment(amendments.get(0), "Mr SeedForenames22005 SeedSurname22005",
                "Mr Changed Forename SeedSurname22005");
            assertAmendment(amendments.get(1), "AliasForenamesSeed AliasSurnameSeed",
                "Changed ForenamesAlias AliasSurnameSeed");
            assertAmendment(amendments.get(2), "AliasForenamesSeed AliasSurnameSeed",
                "AliasForenamesSeed Changed SurnameAlias");

            assertNotNull(lastChangedDateFor(22005L));

            Integer updatedVersion = versionFor(22005L);
            assertEquals(currentVersion + 1, updatedVersion);
        }

        @Test
        @DisplayName("OPAL: PUT Replace DAP – Name and address changes creates multiple amendments.")
        @JiraStory("PO-2471")
        @JiraEpic("PO-1970")
        @JiraTestKey("PO-8768")
        void put_nameAddressChangeCreatesManyAmendments() throws Exception {
            userStateStub.addPermissions((short) 78, FinesPermission.values());

            Integer currentVersion = versionFor(22005L);
            String etag = "\"" + currentVersion + "\"";

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken());
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
                    .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                    .contentType(MediaType.APPLICATION_JSON).content(body));

            String expectedNextEtag = "\"" + (currentVersion + 1) + "\"";

            call.andExpect(status().isOk()).andExpect(header().string(HttpHeaders.ETAG, expectedNextEtag))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.defendant_account_party.defendant_account_party_type").value("Defendant"));

            List<AmendmentEntity> amendments = defendantAccountAmendmentsFor(22005L);

            assertEquals(3, amendments.size());
            assertAmendment(amendments.get(0), "Mr SeedForenames22005 SeedSurname22005",
                "Mr Changed Forenames SeedSurname22005");
            assertAmendment(amendments.get(1), "Seed Address 22005", "Changed Address 22005");
            assertAmendment(amendments.get(2), "SE2 0AA", "NEW PC0");

            assertNotNull(lastChangedDateFor(22005L));

            Integer updatedVersion = versionFor(22005L);
            assertEquals(currentVersion + 1, updatedVersion);
        }

        @Test
        @DisplayName("OPAL: PUT Replace DAP – No field changes creates no amendments")
        @JiraStory("PO-2471")
        @JiraEpic("PO-1970")
        @JiraTestKey("PO-8766")
        void put_noChangeCreatesNoAmendment() throws Exception {
            userStateStub.addPermissions((short) 78, FinesPermission.values());

            Integer currentVersion = versionFor(22005L);
            String etag = "\"" + currentVersion + "\"";

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken());
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
                    .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                    .contentType(MediaType.APPLICATION_JSON).content(body));
            String expectedNextEtag = "\"" + (currentVersion + 1) + "\"";

            call.andExpect(status().isOk()).andExpect(header().string(HttpHeaders.ETAG, expectedNextEtag))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.defendant_account_party.defendant_account_party_type").value("Defendant"));

            List<AmendmentEntity> amendments = defendantAccountAmendmentsFor(22005L);

            assertEquals(0, amendments.size());

            Integer updatedVersion = versionFor(22005L);
            assertEquals(currentVersion + 1, updatedVersion);
        }
    }
}
