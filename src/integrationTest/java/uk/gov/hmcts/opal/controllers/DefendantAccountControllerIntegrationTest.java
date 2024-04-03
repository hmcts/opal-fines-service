package uk.gov.hmcts.opal.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
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
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.service.opal.NoteService;
import uk.gov.hmcts.opal.service.opal.UserStateService;
import uk.gov.hmcts.opal.service.proxy.DefendantAccountServiceProxy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
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
class DefendantAccountControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DefendantAccountServiceProxy defendantAccountService;

    @MockBean
    private NoteService opalNoteService;

    @MockBean
    private UserStateService userStateService;

    @Test
    void testGetDefendantAccountById() throws Exception {
        AccountDetailsDto defendantAccountEntity = createAccountDetailsDto();

        when(userStateService.getUserStateUsingServletRequest(any(HttpServletRequest.class)))
            .thenReturn(new UserState.DeveloperUserState());
        when(defendantAccountService.getAccountDetailsByDefendantAccountId(1L)).thenReturn(defendantAccountEntity);

        mockMvc.perform(get("/api/defendant-account/1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.defendantAccountId").value(1))
            .andExpect(jsonPath("$.accountNumber").value("abc123"))
            .andExpect(jsonPath("$.businessUnitId").value(6));
    }


    @Test
    void testGetDefendantAccountById_WhenDefendantAccountDoesNotExist() throws Exception {
        when(userStateService.getUserStateUsingServletRequest(any(HttpServletRequest.class)))
            .thenReturn(new UserState.DeveloperUserState());
        when(defendantAccountService.getDefendantAccount(any())).thenReturn(null);

        mockMvc.perform(get("/api/defendant-account/2"))
            .andExpect(status().isNoContent());
    }

    @Test
    void testPostDefendantAccountsSearch() throws Exception {
        AccountSummaryDto dto = createAccountSummaryDto();
        AccountSearchResultsDto results = AccountSearchResultsDto.builder().searchResults(List.of(dto)).build();

        when(userStateService.getUserStateUsingServletRequest(any(HttpServletRequest.class)))
            .thenReturn(new UserState.DeveloperUserState());
        when(defendantAccountService.searchDefendantAccounts(any(AccountSearchDto.class))).thenReturn(results);

        mockMvc.perform(post("/api/defendant-account/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"criteria\":\"value\"}"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.pageSize").value(100))
            .andExpect(jsonPath("$.searchResults[0].defendantAccountId").value(1))
            .andExpect(jsonPath("$.searchResults[0].accountNo").value("abc123"))
            .andExpect(jsonPath("$.searchResults[0].name").value("Keith Criminal"))
            .andExpect(jsonPath("$.searchResults[0].court").value("Crown Court"))
            .andExpect(jsonPath("$.searchResults[0].addressLine1").value("1 Central London"));
    }

    @Test
    void testPostDefendantAccountsSearch_WhenDefendantAccountDoesNotExist() throws Exception {
        AccountEnquiryDto dto = AccountEnquiryDto.builder().build();

        when(userStateService.getUserStateUsingServletRequest(any(HttpServletRequest.class)))
            .thenReturn(new UserState.DeveloperUserState());
        when(defendantAccountService.getDefendantAccount(dto)).thenReturn(null);

        mockMvc.perform(post("/api/defendant-account/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"criteria\":\"2\"}"))
            .andExpect(status().isNoContent());
    }

    @Test
    public void testGetDefendantAccount() throws Exception {
        DefendantAccountEntity entity = createDefendantAccountEntity();

        when(userStateService.getUserStateUsingServletRequest(any(HttpServletRequest.class)))
            .thenReturn(new UserState.DeveloperUserState());
        when(defendantAccountService.getDefendantAccount(any())).thenReturn(entity);

        mockMvc.perform(get("/api/defendant-account")
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
    public void testPutDefendantAccount() throws Exception {
        DefendantAccountEntity entity = createDefendantAccountEntity();

        when(userStateService.getUserStateUsingServletRequest(any(HttpServletRequest.class)))
            .thenReturn(new UserState.DeveloperUserState());
        when(defendantAccountService.putDefendantAccount(any())).thenReturn(entity);

        mockMvc.perform(put("/api/defendant-account")
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
    public void testAddNote() throws Exception {
        NoteDto noteDto = createNoteDto();
        AddNoteDto addNoteDto =  AddNoteDto.builder()
            .businessUnitId((short) 123)
            .associatedRecordId("abc123")
            .noteText("Non payment fine")
            .build();

        when(userStateService.getUserStateUsingServletRequest(any(HttpServletRequest.class)))
            .thenReturn(new UserState.DeveloperUserState());
        when(opalNoteService.saveNote(any())).thenReturn(noteDto);

        mockMvc.perform(post("/api/defendant-account/addNote")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(addNoteDto)))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.noteType").value("quick"))
            .andExpect(jsonPath("$.businessUnitId").value(123));
    }

    @Test
    public void testGetNotesForDefendantAccount_notePresent() throws Exception {
        List<NoteDto> notesList = List.of(createNoteDto());

        when(userStateService.getUserStateUsingServletRequest(any(HttpServletRequest.class)))
            .thenReturn(new UserState.DeveloperUserState());
        when(opalNoteService.searchNotes(any())).thenReturn(notesList);

        mockMvc.perform(get("/api/defendant-account/notes/{defendantId}", "dummyDefendantId"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].noteType").value("quick"))
            .andExpect(jsonPath("$[0].noteText").value("A reminder note"))
            .andExpect(jsonPath("$[0].postedBy").value("Vincent"))
            .andExpect(jsonPath("$[0].associatedRecordId").value("abc123"))
            .andExpect(jsonPath("$[0].postedDate").value("2024-03-25 15:30:00"))
            .andExpect(jsonPath("$[0].businessUnitId").value(123));
    }

    @Test
    public void testGetNotesForDefendantAccount_zeroNotes() throws Exception {
        List<NoteDto> notesList = Collections.emptyList();

        when(userStateService.getUserStateUsingServletRequest(any(HttpServletRequest.class)))
            .thenReturn(new UserState.DeveloperUserState());
        when(opalNoteService.searchNotes(any())).thenReturn(notesList);

        mockMvc.perform(get("/api/defendant-account/notes/{defendantId}", "dummyDefendantId"))
            .andExpect(status().isNoContent());
    }


    private AccountDetailsDto createAccountDetailsDto() {
        return AccountDetailsDto.builder()
            .defendantAccountId(1L)
            .accountNumber("abc123")
            .businessUnitId((short)6)
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

    private DefendantAccountEntity createDefendantAccountEntity() {
        return DefendantAccountEntity.builder()
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
            .postedDate(LocalDateTime.of(2024, 3, 25,15,30))
            .businessUnitId((short)123)
            .build();
    }
}
