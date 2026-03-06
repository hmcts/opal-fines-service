package uk.gov.hmcts.opal.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.allFinesPermissionUser;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.dto.PdplIdentifierType;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingCategory;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingLogDetails;

@Slf4j(topic = "opal.DraftAccountControllerIntegrationTest00")
@DisplayName("DraftAccountController Integration Tests")
class DraftAccountControllerIntegrationTestResubmit extends CommonDraftAccountControllerIntegrationTest {

    @Test
    @DisplayName("Re-submit - Defendant only -> Re-submit Draft Account - Defendant PDPL")
    void testResubmitDraftAccount_pdpl_defendantOnly() throws Exception {
        final long draftIdAccount = 105L;

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allFinesPermissionUser());

        ResultActions resultActions = mockMvc.perform(patch(URL_BASE + "/" + draftIdAccount)
            .header("authorization", "Bearer some_value")
            .header("If-Match", "0")
            .contentType(MediaType.APPLICATION_JSON)
            .content(validUpdateRequestBody("65", "Publishing Pending", "X")));

        String response = resultActions.andReturn().getResponse().getContentAsString();

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.draft_account_id").value(draftIdAccount));

        jsonSchemaValidationService.validateOrError(response, GET_DRAFT_ACCOUNT_RESPONSE);

        ArgumentCaptor<PersonalDataProcessingLogDetails> captor =
            ArgumentCaptor.forClass(PersonalDataProcessingLogDetails.class);

        // For defendant-only we expect a single PDPL call (Re-submit Draft Account - Defendant)
        verify(loggingService, timeout(2000).times(1)).personalDataAccessLogAsync(captor.capture());

        PersonalDataProcessingLogDetails pdpl = captor.getValue();
        assertNotNull(pdpl);

        // Full contents assertions (deterministic fields)
        assertEquals("Re-submit Draft Account - Defendant", pdpl.getBusinessIdentifier());
        assertEquals(PersonalDataProcessingCategory.COLLECTION, pdpl.getCategory());
        assertNull(pdpl.getRecipient());
        assertNotNull(pdpl.getCreatedAt());
        assertNotNull(pdpl.getCreatedBy());
        assertEquals(PdplIdentifierType.OPAL_USER_ID, pdpl.getCreatedBy().getType());
        assertNotNull(pdpl.getIndividuals());
        assertEquals(1, pdpl.getIndividuals().size());
        assertEquals(Long.toString(draftIdAccount), pdpl.getIndividuals().getFirst().getIdentifier());
        assertEquals(PdplIdentifierType.DRAFT_ACCOUNT, pdpl.getIndividuals().getFirst().getType());
    }

    @Test
    @DisplayName("Re-submit - pgToPay -> Parent or Guardian then Defendant PDPLs (order)")
    void testResubmitDraftAccount_pdpl_parentOrGuardianThenDefendant() throws Exception {
        final long draftIdAccount = 104L;

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allFinesPermissionUser());

        ResultActions resultActions = mockMvc.perform(patch(URL_BASE + "/" + draftIdAccount)
            .header("authorization", "Bearer some_value")
            .header("If-Match", "0")
            .contentType(MediaType.APPLICATION_JSON)
            .content(validUpdateRequestBody("65", "Publishing Pending", "Y")));

        String response = resultActions.andReturn().getResponse().getContentAsString();

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.draft_account_id").value(draftIdAccount));

        jsonSchemaValidationService.validateOrError(response, GET_DRAFT_ACCOUNT_RESPONSE);

        ArgumentCaptor<PersonalDataProcessingLogDetails> captor =
            ArgumentCaptor.forClass(PersonalDataProcessingLogDetails.class);

        // Expect two calls: Parent or Guardian, then Defendant
        verify(loggingService, timeout(2000).times(2)).personalDataAccessLogAsync(captor.capture());

        List<PersonalDataProcessingLogDetails> calls = captor.getAllValues();
        assertEquals(2, calls.size());

        PersonalDataProcessingLogDetails first = calls.get(0);
        assertNotNull(first);
        assertEquals("Re-submit Draft Account - Parent or Guardian", first.getBusinessIdentifier());
        assertEquals(PersonalDataProcessingCategory.COLLECTION, first.getCategory());
        assertNull(first.getRecipient());
        assertNotNull(first.getCreatedAt());
        assertEquals(Long.toString(draftIdAccount), first.getIndividuals().getFirst().getIdentifier());

        PersonalDataProcessingLogDetails second = calls.get(1);
        assertNotNull(second);
        assertEquals("Re-submit Draft Account - Defendant", second.getBusinessIdentifier());
        assertEquals(PersonalDataProcessingCategory.COLLECTION, second.getCategory());
        assertNull(second.getRecipient());
        assertNotNull(second.getCreatedAt());
        assertEquals(Long.toString(draftIdAccount), second.getIndividuals().getFirst().getIdentifier());
    }

    @Test
    @DisplayName("Re-submit - adultOrYouthOnly WITH minor -> Defendant + Minor Creditor PDPLs (order)")
    void testResubmitDraftAccount_pdpl_defendantAndMinor() throws Exception {
        final long draftIdAccount = 8L; // previously used in your suite; confirm or replace if needed

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allFinesPermissionUser());

        String ifMatch = getIfMatchForDraftAccount(draftIdAccount);
        ResultActions resultActions = mockMvc.perform(patch(URL_BASE + "/" + draftIdAccount)
            .header("authorization", "Bearer some_value")
            .header("If-Match", ifMatch)
            .contentType(MediaType.APPLICATION_JSON)
            .content(validUpdateRequestBody("65", "Publishing Pending", "A")));

        String response = resultActions.andReturn().getResponse().getContentAsString();

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.draft_account_id").value(draftIdAccount))
            .andExpect(jsonPath("$.business_unit_id").value(65));

        jsonSchemaValidationService.validateOrError(response, GET_DRAFT_ACCOUNT_RESPONSE);

        ArgumentCaptor<PersonalDataProcessingLogDetails> captor =
            ArgumentCaptor.forClass(PersonalDataProcessingLogDetails.class);

        // Expect two calls (Defendant then Minor Creditor)
        verify(loggingService, timeout(2000).times(2))
            .personalDataAccessLogAsync(captor.capture());

        List<PersonalDataProcessingLogDetails> calls = captor.getAllValues();
        assertEquals(2, calls.size(), "expected two PDPL log calls");

        // Defendant call
        PersonalDataProcessingLogDetails defendantCall = calls.get(0);
        assertNotNull(defendantCall);
        assertEquals("Re-submit Draft Account - Defendant", defendantCall.getBusinessIdentifier());
        assertEquals(PersonalDataProcessingCategory.COLLECTION, defendantCall.getCategory());
        assertNull(defendantCall.getRecipient());
        assertNotNull(defendantCall.getCreatedAt());
        assertEquals(Long.toString(draftIdAccount), defendantCall.getIndividuals().getFirst().getIdentifier());

        // Minor Creditor call
        PersonalDataProcessingLogDetails minorCall = calls.get(1);
        assertNotNull(minorCall);
        assertEquals("Re-submit Draft Account - Minor Creditor", minorCall.getBusinessIdentifier());
        assertEquals(PersonalDataProcessingCategory.COLLECTION, minorCall.getCategory());
        assertNull(minorCall.getRecipient());
        assertNotNull(minorCall.getCreatedAt());
        assertEquals(Long.toString(draftIdAccount), minorCall.getIndividuals().getFirst().getIdentifier());
    }
}
