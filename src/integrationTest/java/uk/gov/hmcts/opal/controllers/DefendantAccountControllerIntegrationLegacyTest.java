package uk.gov.hmcts.opal.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.SchemaPaths;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.AddNoteDto;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountType;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.court.CourtEntity;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;
import uk.gov.hmcts.opal.service.opal.UserStateService;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles({"integration", "legacy"})
@Slf4j(topic = "opal.DefendantAccountControllerIntegrationTest")
@Sql(scripts = "classpath:db/insertData/insert_into_defendants.sql", executionPhase = BEFORE_TEST_CLASS)
@DisplayName("Defendant Account Controller Integration Tests - Legacy")
class DefendantAccountControllerIntegrationLegacyTest extends AbstractIntegrationTest {
    @MockitoSpyBean
    private JsonSchemaValidationService jsonSchemaValidationService;
    private static final String URL_BASE = "/defendant-accounts/";

    @MockitoBean
    private UserStateService userStateService;

    @Test
    @DisplayName("Get Defendant Account by ID [@PO-33, @PO-130]")
    void testGetDefendantAccountById() throws Exception {
        when(userStateService.getUserStateUsingAuthToken(anyString()))
            .thenReturn(new UserState.DeveloperUserState());

        ResultActions actions = mockMvc.perform(get(URL_BASE + "1")
            .header("authorization", "Bearer some_value"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testGetDefendantAccountById: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.defendant_account_id").value(1))
            .andExpect(jsonPath("$.account_number").value("100A"))
            .andExpect(jsonPath("$.business_unit_id").value(78));
    }

    @Test
    @DisplayName("Get Defendant Account by ID - Account does not exist [@PO-33, @PO-130]")
    void testGetDefendantAccountById_WhenDefendantAccountDoesNotExist() throws Exception {
        when(userStateService.getUserStateUsingAuthToken(anyString()))
            .thenReturn(new UserState.DeveloperUserState());

        mockMvc.perform(get(URL_BASE + "2")
                .header("authorization", "Bearer some_value"))
            .andExpect(status().isNotFound());
    }

    @Test
    @Disabled("Disabled until DTSPO-27066 is resolved")
    @DisplayName("Search defendant accounts - POST with valid criteria [@PO-1901, @PO-33, @PO-119]")
    void testPostDefendantAccountsSearch() throws Exception {
        when(userStateService.getUserStateUsingAuthToken(anyString()))
            .thenReturn(new UserState.DeveloperUserState());

        ResultActions actions = mockMvc.perform(post(URL_BASE + "search")
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                              "active_accounts_only": true,
                              "business_unit_ids": [78],
                              "reference_number": null,
                              "defendant": {
                                "organisation": true,
                                "organisation_name": "Acme Ltd",
                                "exact_match_organisation_name": true,
                                "include_aliases": true,
                                "address_line_1": "Lumber House",
                                "postcode": "AB1 2CD",
                                "surname": null,
                                "exact_match_surname": null,
                                "forenames": null,
                                "exact_match_forenames": null,
                                "birth_date": null,
                                "national_insurance_number": null
                              }
                            }
                       """));


        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDefendantAccountsSearch: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("1"))
            .andExpect(jsonPath("$.defendant_accounts[0].account_number").value("100A"))
            .andExpect(jsonPath("$.defendant_accounts[0].organisation").value(true))
            .andExpect(jsonPath("$.defendant_accounts[0].organisation_name").value("Acme Ltd"))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_title").value(IsNull.nullValue()))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_firstnames").value(IsNull.nullValue()))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_surname").value(IsNull.nullValue()))
            .andExpect(jsonPath("$.defendant_accounts[0].address_line_1").value("Lumber House"))
            .andExpect(jsonPath("$.defendant_accounts[0].postcode").value("AB1 2CD"))
            .andExpect(jsonPath("$.defendant_accounts[0].business_unit_name").value("Business Unit A"));

        assertTrue(jsonSchemaValidationService.isValid(body, SchemaPaths.POST_DEFENDANT_ACCOUNT_SEARCH_RESPONSE));

    }

    @Test
    @DisplayName("Search defendant accounts - Invalid payload with both reference_number and defendant")
    void testPostDefendantAccountsSearch_InvalidPayload() throws Exception {
        mockMvc.perform(post(URL_BASE + "search")
                .header("authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
              "active_accounts_only": true,
              "business_unit_ids": [78],
              "reference_number": {
                "organisation": true,
                "account_number": "12345",
                "prosecutor_case_reference": null
              },
              "defendant": {
                "include_aliases": true,
                "organisation": false,
                "surname": "Smith"
              }
            }
        """))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Search defendant accounts - Missing required fields")
    void testPostDefendantAccountsSearch_MissingFields() throws Exception {
        mockMvc.perform(post(URL_BASE + "search")
                .header("authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
              "business_unit_ids": [78]
            }
        """))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Search defendant accounts - Empty payload")
    void testPostDefendantAccountsSearch_EmptyPayload() throws Exception {
        mockMvc.perform(post(URL_BASE + "search")
                .header("authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Disabled("Disabled until DTSPO-27066 is resolved")
    @DisplayName("Search defendant accounts - No Accounts found [@PO-1901, @PO-33, @PO-119]")
    void testPostDefendantAccountsSearch_WhenNoDefendantAccountsFound() throws Exception {
        when(userStateService.getUserStateUsingAuthToken(anyString()))
            .thenReturn(new UserState.DeveloperUserState());

        ResultActions actions = mockMvc.perform(post(URL_BASE + "search")
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "active_accounts_only": true,
                  "business_unit_ids": [999999],
                  "reference_number": null,
                  "defendant": {
                    "organisation": false,
                    "include_aliases": false,
                    "surname": "ShouldNotMatchAnythingXYZ",
                    "exact_match_surname": true,
                    "forenames": null,
                    "exact_match_forenames": null,
                    "address_line_1": null,
                    "postcode": null,
                    "organisation_name": null,
                    "exact_match_organisation_name": null,
                    "birth_date": null,
                    "national_insurance_number": null
                  }
                }
                    """));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDefendantAccountsSearch_WhenNoDefendantAccountsFound: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(0));
    }

    @Test
    @DisplayName("Search defendant accounts - user lacks permissions [@PO-1901]")
    void testPostDefendantAccountsSearch_WhenUserLacksPermissions() throws Exception {
        when(userStateService.getUserStateUsingAuthToken(anyString()))
            .thenReturn(new UserState(999L, "unauth_user", Collections.emptySet()));

        ResultActions actions = mockMvc.perform(post(URL_BASE + "search")
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
            {
              "active_accounts_only": true,
              "business_unit_ids": [78],
              "reference_number": null,
              "defendant": {
                "organisation": false,
                "include_aliases": false,
                "surname": "Smith",
                "exact_match_surname": true,
                "forenames": null,
                "exact_match_forenames": null,
                "address_line_1": null,
                "postcode": null,
                "organisation_name": null,
                "exact_match_organisation_name": null,
                "birth_date": null,
                "national_insurance_number": null
              }
            }
            """));

        actions.andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Search defendant accounts - Account does exist [@PO-33, @PO-119]")
    public void testGetDefendantAccount() throws Exception {
        when(userStateService.getUserStateUsingAuthToken(anyString()))
            .thenReturn(new UserState.DeveloperUserState());

        ResultActions actions = mockMvc.perform(get("/defendant-accounts")
            .header("authorization", "Bearer some_value")
            .param("businessUnitId", "78")
            .param("accountNumber", "100A"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testGetDefendantAccount: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.defendantAccountId").value(1))
            .andExpect(jsonPath("$.accountNumber").value("100A"))
            .andExpect(jsonPath("$.accountStatus").value("L"))
            .andExpect(jsonPath("$.accountBalance").value(500.58))
            .andExpect(jsonPath("$.amountPaid").value(200.0));
    }

    @Test
    @DisplayName("Test Add Note Endpoint [@PO-34, @PO-138]")
    public void testAddNote() throws Exception {
        AddNoteDto addNoteDto = AddNoteDto.builder()
            .businessUnitId((short) 123)
            .associatedRecordId("abc123")
            .noteText("Non payment fine")
            .build();

        when(userStateService.getUserStateUsingAuthToken(anyString()))
            .thenReturn(new UserState.DeveloperUserState());

        ResultActions actions = mockMvc.perform(post(URL_BASE + "addNote")
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(addNoteDto)));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testAddNote: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.note_type").value("AA"))
            .andExpect(jsonPath("$.business_unit_id").value(123));
    }

    @Test
    @DisplayName("Get notes for defendant account - Note present [@PO-34, @PO-138]")
    public void testGetNotesForDefendantAccount_notePresent() throws Exception {
        when(userStateService.getUserStateUsingAuthToken(anyString()))
            .thenReturn(new UserState.DeveloperUserState());

        ResultActions actions = mockMvc.perform(get(URL_BASE + "notes/{defendantId}", "1")
            .header("authorization", "Bearer some_value"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testGetNotesForDefendantAccount_notePresent: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].note_type").value("AC"))
            .andExpect(jsonPath("$[0].note_text").value("Comment for Notes for Ms Anna Graham"))
            .andExpect(jsonPath("$[0].posted_by").value("Dr Notes"))
            .andExpect(jsonPath("$[0].associated_record_id").value("1"))
            .andExpect(jsonPath("$[0].posted_date").value(IsNull.nullValue()))
            .andExpect(jsonPath("$[0].business_unit_id").value(IsNull.nullValue()));
    }

    @Test
    public void testGetNotesForDefendantAccount_zeroNotes() throws Exception {
        when(userStateService.getUserStateUsingAuthToken(anyString()))
            .thenReturn(new UserState.DeveloperUserState());

        mockMvc.perform(get(URL_BASE + "notes/{defendantId}", "dummyDefendantId")
                .header("authorization", "Bearer some_value"))
            .andExpect(status().isOk());
    }

    private DefendantAccountEntity createDefendantAccountEntity() {
        return DefendantAccountEntity.builder()
            .defendantAccountId(1L)
            .businessUnit(BusinessUnitEntity.builder().businessUnitId((short)78).build())
            .accountNumber("abc123")
            .accountStatus("IP")
            .accountType(DefendantAccountType.FINES)
            .enforcingCourt(CourtEntity.builder().courtId(780000000185L).build())
            .lastHearingCourt(CourtEntity.builder().courtId(780000000186L).build())
            .amountImposed(BigDecimal.TEN)
            .accountBalance(BigDecimal.TEN)
            .amountPaid(BigDecimal.ONE)
            .build();
    }
}
