package uk.gov.hmcts.opal.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.AddNoteDto;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountType;
import uk.gov.hmcts.opal.entity.court.CourtEntity;
import uk.gov.hmcts.opal.service.opal.UserStateService;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles({"integration"})
@Slf4j(topic = "opal.DiscoDefendantAccountControllerIntegrationTest")
@Sql(scripts = "classpath:db/insertData/insert_into_defendants.sql", executionPhase = BEFORE_TEST_CLASS)
@DisplayName("Defendant Account Controller Integration Tests")
class DiscoDefendantAccountControllerIntegrationTest extends AbstractIntegrationTest {

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
    @DisplayName("Search defendant accounts - POST with valid criteria [@PO-33, @PO-119]")
    void testPostDefendantAccountsSearch() throws Exception {
        when(userStateService.getUserStateUsingAuthToken(anyString()))
            .thenReturn(new UserState.DeveloperUserState());

        ResultActions actions = mockMvc.perform(post(URL_BASE + "search")
                            .header("authorization", "Bearer some_value")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"criteria\":\"value\"}"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDefendantAccountsSearch: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.page_size").value(100))
            .andExpect(jsonPath("$.search_results[0].account_no").value("100A"))
            .andExpect(jsonPath("$.search_results[0].name").value("Ms Anna Graham"))
            .andExpect(jsonPath("$.search_results[0].court").value("780000000185"))
            .andExpect(jsonPath("$.search_results[0].address_line_1").value("Lumber House"));
    }

    @Test
    @DisplayName("Search defendant accounts - No Accounts found [@PO-33, @PO-119]")
    void testPostDefendantAccountsSearch_WhenNoDefendantAccountsFound() throws Exception {
        when(userStateService.getUserStateUsingAuthToken(anyString()))
            .thenReturn(new UserState.DeveloperUserState());

        ResultActions actions = mockMvc.perform(post(URL_BASE + "search")
                            .header("authorization", "Bearer some_value")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"surname\":\"Wilson\"}"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDefendantAccountsSearch_WhenNoDefendantAccountsFound: Response body:\n{}",
                 ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(0));
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
    @DisplayName("Update defendant account ")
    public void testPutDefendantAccount() throws Exception {
        DefendantAccountEntity entity = createDefendantAccountEntity();

        when(userStateService.getUserStateUsingAuthToken(anyString()))
            .thenReturn(new UserState.DeveloperUserState());

        ResultActions actions = mockMvc.perform(put("/defendant-accounts")
                            .header("authorization", "Bearer some_value")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(entity)));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPutDefendantAccount: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.defendantAccountId").value(1L))
            .andExpect(jsonPath("$.accountNumber").value("100A"))
            .andExpect(jsonPath("$.accountStatus").value("L"))
            .andExpect(jsonPath("$.accountBalance").value(500.58))
            .andExpect(jsonPath("$.amountPaid").value(200));
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
