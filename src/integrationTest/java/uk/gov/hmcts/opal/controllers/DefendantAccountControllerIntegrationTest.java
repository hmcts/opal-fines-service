package uk.gov.hmcts.opal.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.AccountDetailsDto;
import uk.gov.hmcts.opal.dto.AccountEnquiryDto;
import uk.gov.hmcts.opal.dto.AccountSummaryDto;
import uk.gov.hmcts.opal.dto.AddNoteDto;
import uk.gov.hmcts.opal.dto.NoteDto;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.AccountSearchResultsDto;
import uk.gov.hmcts.opal.entity.defendant.DefendantAccountCore;
import uk.gov.hmcts.opal.service.opal.NoteService;
import uk.gov.hmcts.opal.service.opal.UserStateService;
import uk.gov.hmcts.opal.service.proxy.DefendantAccountServiceProxy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = DefendantAccountController.class)
@ActiveProfiles({"integration"})
@DisplayName("Defendant Account Controller Integration Tests")
class DefendantAccountControllerIntegrationTest {

    private static final String URL_BASE = "/defendant-accounts/";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DefendantAccountServiceProxy defendantAccountService;

    @MockBean
    private NoteService opalNoteService;

    @MockBean
    private UserStateService userStateService;

    @Test
    @DisplayName("Get Defendant Account by ID [@PO-33, @PO-130]")
    void testGetDefendantAccountById() throws Exception {
        AccountDetailsDto defendantAccountEntity = createAccountDetailsDto();

        when(userStateService.getUserStateUsingAuthToken(anyString()))
            .thenReturn(new UserState.DeveloperUserState());
        when(defendantAccountService.getAccountDetailsByDefendantAccountId(1L)).thenReturn(defendantAccountEntity);

        mockMvc.perform(get(URL_BASE + "1")
                            .header("authorization", "Bearer some_value"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.defendant_account_id").value(1))
            .andExpect(jsonPath("$.account_number").value("abc123"))
            .andExpect(jsonPath("$.business_unit_id").value(6));
    }


    @Test
    @DisplayName("Get Defendant Account by ID - Account does not exist [@PO-33, @PO-130]")
    void testGetDefendantAccountById_WhenDefendantAccountDoesNotExist() throws Exception {
        when(userStateService.getUserStateUsingAuthToken(anyString()))
            .thenReturn(new UserState.DeveloperUserState());
        when(defendantAccountService.getDefendantAccount(any())).thenReturn(null);

        mockMvc.perform(get(URL_BASE + "2")
                            .header("authorization", "Bearer some_value"))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Search defendant accounts - POST with valid criteria [@PO-33, @PO-119]")
    void testPostDefendantAccountsSearch() throws Exception {
        AccountSummaryDto dto = createAccountSummaryDto();
        AccountSearchResultsDto results = AccountSearchResultsDto.builder().searchResults(List.of(dto)).build();

        when(userStateService.getUserStateUsingAuthToken(anyString()))
            .thenReturn(new UserState.DeveloperUserState());
        when(defendantAccountService.searchDefendantAccounts(any(AccountSearchDto.class))).thenReturn(results);

        mockMvc.perform(post(URL_BASE + "search")
                            .header("authorization", "Bearer some_value")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"criteria\":\"value\"}"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.page_size").value(100))
            .andExpect(jsonPath("$.search_results[0].defendant_account_id").value(1))
            .andExpect(jsonPath("$.search_results[0].account_no").value("abc123"))
            .andExpect(jsonPath("$.search_results[0].name").value("Keith Criminal"))
            .andExpect(jsonPath("$.search_results[0].court").value("Crown Court"))
            .andExpect(jsonPath("$.search_results[0].address_line_1").value("1 Central London"));
    }

    @Test
    @DisplayName("Search defendant accounts - Account does not exist [@PO-33, @PO-119]")
    void testPostDefendantAccountsSearch_WhenDefendantAccountDoesNotExist() throws Exception {
        AccountEnquiryDto dto = AccountEnquiryDto.builder().build();

        when(userStateService.getUserStateUsingAuthToken(anyString()))
            .thenReturn(new UserState.DeveloperUserState());
        when(defendantAccountService.getDefendantAccount(dto)).thenReturn(null);

        mockMvc.perform(post(URL_BASE + "search")
                            .header("authorization", "Bearer some_value")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"criteria\":\"2\"}"))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Search defendant accounts - Account does exist [@PO-33, @PO-119]")
    public void testGetDefendantAccount() throws Exception {
        DefendantAccountCore entity = createDefendantAccountEntity();

        when(userStateService.getUserStateUsingAuthToken(anyString()))
            .thenReturn(new UserState.DeveloperUserState());
        when(defendantAccountService.getDefendantAccount(any())).thenReturn(entity);

        mockMvc.perform(get("/defendant-accounts")
                            .header("authorization", "Bearer some_value")
                            .param("businessUnitId", "1")
                            .param("accountNumber", "123"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.defendantAccountId").value(1))
            .andExpect(jsonPath("$.accountNumber").value("abc123"))
            .andExpect(jsonPath("$.accountStatus").value("in payment"))
            .andExpect(jsonPath("$.accountBalance").value(10))
            .andExpect(jsonPath("$.amountPaid").value(1));
    }

    @Test
    @DisplayName("Update defendant account ")
    public void testPutDefendantAccount() throws Exception {
        DefendantAccountCore entity = createDefendantAccountEntity();

        when(userStateService.getUserStateUsingAuthToken(anyString()))
            .thenReturn(new UserState.DeveloperUserState());
        when(defendantAccountService.putDefendantAccount(any(DefendantAccountCore.class))).thenReturn(entity);

        mockMvc.perform(put("/defendant-accounts")
                            .header("authorization", "Bearer some_value")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(entity)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.defendantAccountId").value(1))
            .andExpect(jsonPath("$.accountNumber").value("abc123"))
            .andExpect(jsonPath("$.accountStatus").value("in payment"))
            .andExpect(jsonPath("$.accountBalance").value(10))
            .andExpect(jsonPath("$.amountPaid").value(1));
    }


    @Test
    @DisplayName("Test Add Note Endpoint [@PO-34, @PO-138]")
    public void testAddNote() throws Exception {
        NoteDto noteDto = createNoteDto();
        AddNoteDto addNoteDto = AddNoteDto.builder()
            .businessUnitId((short) 123)
            .associatedRecordId("abc123")
            .noteText("Non payment fine")
            .build();

        when(userStateService.getUserStateUsingAuthToken(anyString()))
            .thenReturn(new UserState.DeveloperUserState());
        when(opalNoteService.saveNote(any())).thenReturn(noteDto);

        mockMvc.perform(post(URL_BASE + "addNote")
                            .header("authorization", "Bearer some_value")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(addNoteDto)))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.note_type").value("quick"))
            .andExpect(jsonPath("$.business_unit_id").value(123));
    }

    @Test
    @DisplayName("Get notes for defendant account - Note present [@PO-34, @PO-138]")
    public void testGetNotesForDefendantAccount_notePresent() throws Exception {
        List<NoteDto> notesList = List.of(createNoteDto());

        when(userStateService.getUserStateUsingAuthToken(anyString()))
            .thenReturn(new UserState.DeveloperUserState());
        when(opalNoteService.searchNotes(any())).thenReturn(notesList);

        mockMvc.perform(get(URL_BASE + "notes/{defendantId}", "dummyDefendantId")
                            .header("authorization", "Bearer some_value"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].note_type").value("quick"))
            .andExpect(jsonPath("$[0].note_text").value("A reminder note"))
            .andExpect(jsonPath("$[0].posted_by").value("Vincent"))
            .andExpect(jsonPath("$[0].associated_record_id").value("abc123"))
            .andExpect(jsonPath("$[0].posted_date").value("2024-03-25 15:30:00"))
            .andExpect(jsonPath("$[0].business_unit_id").value(123));
    }

    @Test
    public void testGetNotesForDefendantAccount_zeroNotes() throws Exception {
        List<NoteDto> notesList = Collections.emptyList();

        when(userStateService.getUserStateUsingAuthToken(anyString()))
            .thenReturn(new UserState.DeveloperUserState());
        when(opalNoteService.searchNotes(any())).thenReturn(notesList);

        mockMvc.perform(get(URL_BASE + "notes/{defendantId}", "dummyDefendantId")
                            .header("authorization", "Bearer some_value"))
            .andExpect(status().isOk());
    }


    private AccountDetailsDto createAccountDetailsDto() {
        return AccountDetailsDto.builder()
            .defendantAccountId(1L)
            .accountNumber("abc123")
            .businessUnitId((short) 6)
            .build();
    }

    private AccountSummaryDto createAccountSummaryDto() {
        return AccountSummaryDto.builder()
            .defendantAccountId(1L)
            .accountNo("abc123")
            .name("Keith Criminal")
            .court("Crown Court")
            .addressLine1("1 Central London")
            .build();
    }

    private DefendantAccountCore createDefendantAccountEntity() {
        return DefendantAccountCore.builder()
            .defendantAccountId(1L)
            .accountNumber("abc123")
            .accountStatus("in payment")
            .accountBalance(BigDecimal.TEN)
            .amountPaid(BigDecimal.ONE)
            .build();
    }

    private NoteDto createNoteDto() {
        return NoteDto.builder()
            .noteId(9L)
            .noteType("quick")
            .noteText("A reminder note")
            .postedBy("Vincent")
            .associatedRecordId("abc123")
            .postedDate(LocalDateTime.of(2024, 3, 25, 15, 30))
            .businessUnitId((short) 123)
            .build();
    }
}
