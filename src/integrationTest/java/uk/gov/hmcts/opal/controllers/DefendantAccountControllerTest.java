package uk.gov.hmcts.opal.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.opal.dto.AccountDetailsDto;
import uk.gov.hmcts.opal.dto.AddNoteDto;
import uk.gov.hmcts.opal.dto.NoteDto;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.AccountSearchResultsDto;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.service.opal.NoteService;
import uk.gov.hmcts.opal.service.opal.UserStateService;
import uk.gov.hmcts.opal.service.proxy.DefendantAccountServiceProxy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = DefendantAccountController.class)
@ActiveProfiles({"integration"})
public class DefendantAccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DefendantAccountServiceProxy defendantAccountService;

    @MockBean
    private NoteService opalNoteService;

    @MockBean
    private UserStateService userStateService;

    @Test
    public void testGetDefendantAccount() throws Exception {
        DefendantAccountEntity dummyEntity = DefendantAccountEntity.builder().build();

        when(defendantAccountService.getDefendantAccount(any())).thenReturn(dummyEntity);

        mockMvc.perform(get("/api/defendant-account")
                            .param("businessUnitId", "1")
                            .param("accountNumber", "123"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testPutDefendantAccount() throws Exception {
        DefendantAccountEntity dummyEntity = DefendantAccountEntity.builder().build();

        when(defendantAccountService.putDefendantAccount(any())).thenReturn(dummyEntity);

        mockMvc.perform(put("/api/defendant-account")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(dummyEntity)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testGetAccountDetails() throws Exception {
        AccountDetailsDto dummyDto = new AccountDetailsDto();

        when(defendantAccountService.getAccountDetailsByDefendantAccountId(any())).thenReturn(dummyDto);

        mockMvc.perform(get("/api/defendant-account/{defendantAccountId}", 123))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testPostDefendantAccountSearch() throws Exception {
        AccountSearchResultsDto dummyResultDto = AccountSearchResultsDto.builder()
            .searchResults(Collections.emptyList())
            .build();

        when(defendantAccountService.searchDefendantAccounts(any())).thenReturn(dummyResultDto);

        mockMvc.perform(post("/api/defendant-account/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(AccountSearchDto
                                                                               .builder()
                                                                               .forename("forename")
                                                                               .build())))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testAddNote() throws Exception {
        NoteDto dummyNoteDto = NoteDto.builder().build();

        when(opalNoteService.saveNote(any())).thenReturn(dummyNoteDto);

        mockMvc.perform(post("/api/defendant-account/addNote")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(AddNoteDto.builder()
                                                                               .businessUnitId((short) 123)
                                                                               .associatedRecordId("recordId")
                                                                               .noteText("my note")
                                                                               .build())))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testGetNotesForDefendantAccount() throws Exception {
        List<NoteDto> dummyNotes = new ArrayList<>();

        when(opalNoteService.searchNotes(any())).thenReturn(dummyNotes);

        mockMvc.perform(get("/api/defendant-account/notes/{defendantId}", "dummyDefendantId"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

}
