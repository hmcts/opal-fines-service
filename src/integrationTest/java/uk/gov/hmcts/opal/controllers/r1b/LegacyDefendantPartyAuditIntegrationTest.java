package uk.gov.hmcts.opal.controllers.r1b;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.legacy.service.GatewayService;
import uk.gov.hmcts.opal.dto.legacy.AddressDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.DefendantAccountPartyLegacy;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountPartyLegacyRequest;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountPartyLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.IndividualDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.LegacyReplaceDefendantAccountPartyRequest;
import uk.gov.hmcts.opal.dto.legacy.LegacyReplaceDefendantAccountPartyResponse;
import uk.gov.hmcts.opal.dto.legacy.PartyDetailsLegacy;
import uk.gov.hmcts.opal.service.legacy.LegacyDefendantAccountPartyService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles({"integration", "legacy"})
@TestPropertySource(properties = {
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1b=true"
})
@Sql(scripts = "classpath:db/insertData/insert_into_defendant_accounts_put_methods.sql",
    executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:db/deleteData/delete_from_defendant_accounts_put_methods.sql",
    executionPhase = AFTER_TEST_METHOD)
class LegacyDefendantPartyAuditIntegrationTest extends AbstractIntegrationTest {

    private static final long DEFENDANT_ACCOUNT_ID = 22005L;
    private static final long DEFENDANT_ACCOUNT_PARTY_ID = 22005L;
    private static final short BUSINESS_UNIT_ID = 78;
    private static final String UPDATED_FORENAME = "Updated";
    private static final String EXPECTED_NEW_NAME = "Mr Updated SeedSurname22005";

    @MockitoBean
    private GatewayService gatewayService;

    @Test
    @JiraEpic("PO-812")
    @JiraStory("PO-10780")
    @DisplayName("PO-10780 legacy party replacement tracks first-name update in party details and audit")
    void replaceDefendantParty_whenLegacyMode_tracksFirstNameUpdateInPartyDetailsAndAudit() throws Exception {
        userStateStub.addPermissions(BUSINESS_UNIT_ID, FinesPermission.values());
        stubLegacyPartyReplacement();
        stubLegacyPartyRead();

        mockMvc.perform(put("/defendant-accounts/{accountId}/defendant-account-parties/{partyId}",
                            DEFENDANT_ACCOUNT_ID, DEFENDANT_ACCOUNT_PARTY_ID)
                            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                            .headers(headers())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(replacePartyRequest()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.defendant_account_party.party_details.individual_details.forenames")
                .value(UPDATED_FORENAME));

        mockMvc.perform(get("/defendant-accounts/{accountId}/defendant-account-parties/{partyId}",
                            DEFENDANT_ACCOUNT_ID, DEFENDANT_ACCOUNT_PARTY_ID)
                            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                            .header(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken())
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.defendant_account_party.party_details.individual_details.forenames")
                .value(UPDATED_FORENAME));

        mockMvc.perform(post("/amendments/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                  "associated_record_type": "defendant_accounts",
                                  "associated_record_id": "%s",
                                  "function_code": "ACCOUNT_ENQUIRY"
                                }
                                """.formatted(DEFENDANT_ACCOUNT_ID)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.searchData[0].associated_record_type").value("defendant_accounts"))
            .andExpect(jsonPath("$.searchData[0].associated_record_id").value(String.valueOf(DEFENDANT_ACCOUNT_ID)))
            .andExpect(jsonPath("$.searchData[0].function_code").value("ACCOUNT_ENQUIRY"))
            .andExpect(jsonPath("$.searchData[0].old_value").value("Mr SeedForenames22005 SeedSurname22005"))
            .andExpect(jsonPath("$.searchData[0].new_value").value(EXPECTED_NEW_NAME))
            .andExpect(jsonPath("$.searchData[0].new_value").value(containsString(UPDATED_FORENAME)));
    }

    private void stubLegacyPartyReplacement() {
        when(gatewayService.postToGateway(
            eq(LegacyDefendantAccountPartyService.REPLACE_DEFENDANT_ACCOUNT_PARTY),
            eq(LegacyReplaceDefendantAccountPartyResponse.class),
            any(LegacyReplaceDefendantAccountPartyRequest.class),
            isNull()
        )).thenAnswer(invocation -> {
            int updatedRows = jdbcTemplate.update(
                "UPDATE parties SET forenames = ? WHERE party_id = ?",
                UPDATED_FORENAME,
                DEFENDANT_ACCOUNT_PARTY_ID
            );
            assertEquals(1, updatedRows);

            return new GatewayService.Response<>(
                HttpStatus.OK,
                legacyReplaceResponse(),
                null,
                null
            );
        });
    }

    private void stubLegacyPartyRead() {
        when(gatewayService.postToGateway(
            eq(LegacyDefendantAccountPartyService.GET_DEFENDANT_ACCOUNT_PARTY),
            eq(GetDefendantAccountPartyLegacyResponse.class),
            any(GetDefendantAccountPartyLegacyRequest.class),
            isNull()
        )).thenReturn(new GatewayService.Response<>(
            HttpStatus.OK,
            GetDefendantAccountPartyLegacyResponse.builder()
                .version(BigInteger.ONE)
                .defendantAccountParty(updatedDefendantAccountParty())
                .build(),
            null,
            null
        ));
    }

    private HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken());
        headers.add("Business-Unit-Id", String.valueOf(BUSINESS_UNIT_ID));
        headers.add(HttpHeaders.IF_MATCH, "\"0\"");
        return headers;
    }

    private String replacePartyRequest() {
        return """
            {
              "defendant_account_party_type": "Defendant",
              "is_debtor": true,
              "party_details": {
                "party_id": "22005",
                "organisation_flag": false,
                "individual_details": {
                  "title": "Mr",
                  "forenames": "Updated",
                  "surname": "SeedSurname22005",
                  "date_of_birth": "1990-01-01",
                  "age": null,
                  "national_insurance_number": "SNI22005",
                  "individual_aliases": [
                    {
                      "alias_id": "2200501",
                      "sequence_number": 1,
                      "forenames": "AliasForenamesSeed",
                      "surname": "AliasSurnameSeed"
                    },
                    {
                      "alias_id": "2200502",
                      "sequence_number": 2,
                      "forenames": "AliasForenamesSeed",
                      "surname": "AliasSurnameSeed"
                    }
                  ]
                }
              },
              "address": {
                "address_line_1": "Seed Address 22005",
                "address_line_2": null,
                "address_line_3": null,
                "address_line_4": null,
                "address_line_5": null,
                "postcode": "SE2 0AA"
              },
              "contact_details": null,
              "vehicle_details": null,
              "employer_details": null,
              "language_preferences": null
            }
            """;
    }

    private LegacyReplaceDefendantAccountPartyResponse legacyReplaceResponse() {
        return LegacyReplaceDefendantAccountPartyResponse.builder()
            .version(BigInteger.ONE)
            .defendantAccountParty(updatedDefendantAccountParty())
            .build();
    }

    private DefendantAccountPartyLegacy updatedDefendantAccountParty() {
        return DefendantAccountPartyLegacy.builder()
            .defendantAccountPartyType("Defendant")
            .isDebtor(true)
            .partyDetails(PartyDetailsLegacy.builder()
                .partyId(String.valueOf(DEFENDANT_ACCOUNT_PARTY_ID))
                .organisationFlag(false)
                .individualDetails(IndividualDetailsLegacy.builder()
                    .title("Mr")
                    .forenames(UPDATED_FORENAME)
                    .surname("SeedSurname22005")
                    .dateOfBirth("1990-01-01")
                    .nationalInsuranceNumber("SNI22005")
                    .build())
                .build())
            .address(AddressDetailsLegacy.builder()
                .addressLine1("Seed Address 22005")
                .postcode("SE2 0AA")
                .build())
            .build();
    }
}
