package uk.gov.hmcts.opal.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.opal.dto.search.DraftAccountSearchDto;
import uk.gov.hmcts.opal.entity.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.DraftAccountEntity;
import uk.gov.hmcts.opal.service.opal.DraftAccountService;
import uk.gov.hmcts.opal.service.opal.UserStateService;

import java.time.LocalDate;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = DraftAccountController.class)
@ActiveProfiles({"integration"})
class DraftAccountControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    @Qualifier("draftAccountService")
    DraftAccountService draftAccountService;

    @MockBean
    UserStateService userStateService;

    @Test
    void testGetDraftAccountById() throws Exception {
        DraftAccountEntity draftAccountEntity = createDraftAccountEntity();

        when(draftAccountService.getDraftAccount(1L)).thenReturn(draftAccountEntity);

        mockMvc.perform(get("/api/draft-account/1")
                            .header("authorization", "Bearer some_value"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.draftAccountId").value(1))
            .andExpect(jsonPath("$.businessUnit.businessUnitId").value(7))
            .andExpect(jsonPath("$.accountType").value("DRAFT"))
            .andExpect(jsonPath("$.createdBy").value("Tony"))
            .andExpect(jsonPath("$.accountStatus").value("CREATED"));
    }


    @Test
    void testGetDraftAccountById_WhenDraftAccountDoesNotExist() throws Exception {
        when(draftAccountService.getDraftAccount(2L)).thenReturn(null);

        mockMvc.perform(get("/api/draft-account/2").header("authorization", "Bearer some_value"))
            .andExpect(status().isNoContent());
    }

    @Test
    void testPostDraftAccountsSearch() throws Exception {
        DraftAccountEntity draftAccountEntity = createDraftAccountEntity();

        when(draftAccountService.searchDraftAccounts(any(DraftAccountSearchDto.class)))
            .thenReturn(singletonList(draftAccountEntity));

        mockMvc.perform(post("/api/draft-account/search")
                            .header("authorization", "Bearer some_value")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"criteria\":\"value\"}"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].draftAccountId").value(1))
            .andExpect(jsonPath("$[0].businessUnit.businessUnitId").value(7))
            .andExpect(jsonPath("$[0].accountType").value("DRAFT"))
            .andExpect(jsonPath("$[0].createdBy").value("Tony"))
            .andExpect(jsonPath("$[0].accountStatus").value("CREATED"));
    }

    @Test
    void testPostDraftAccountsSearch_WhenDraftAccountDoesNotExist() throws Exception {
        mockMvc.perform(post("/api/draft-account/search")
                            .header("authorization", "Bearer some_value")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"criteria\":\"2\"}"))
            .andExpect(status().isNoContent());
    }

    private DraftAccountEntity createDraftAccountEntity() {
        return DraftAccountEntity.builder()
            .draftAccountId(1L)
            .businessUnit(BusinessUnitEntity.builder().businessUnitId((short)007).build())
            .createdDate(LocalDate.of(2023, 1, 2))
            .createdBy("Tony")
            .accountType("DRAFT")
            .accountStatus("CREATED")
            .build();
    }
}
