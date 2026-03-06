package uk.gov.hmcts.opal.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.allFinesPermissionUser;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.noFinesPermissionUser;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.permissionUser;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.dto.ToJsonString;

@Slf4j(topic = "opal.DraftAccountControllerIntegrationTest00")
@DisplayName("DraftAccountController Integration Tests")
class DraftAccountControllerIntegrationTestUpdate extends CommonDraftAccountControllerIntegrationTest {

    @Test
    @DisplayName("Update draft account - Should return updated account details [@PO-973, @PO-745]")
    void testUpdateDraftAccount_success() throws Exception {
        Long draftAccountId = 8L; // not touched by any other PATCH/PUT test
        when(userStateService.checkForAuthorisedUser(any()))
            .thenReturn(permissionUser((short) 65, FinesPermission.CHECK_VALIDATE_DRAFT_ACCOUNTS));

        ResultActions resultActions = mockMvc.perform(patch(URL_BASE + "/" + draftAccountId)
            .header("authorization", "Bearer some_value")
            .header("If-Match", "0")
            .contentType(MediaType.APPLICATION_JSON)
            .content(validUpdateRequestBody("65", "Publishing Pending","A")));

        String response = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testUpdateDraftAccount_success: Response body:\n{}", ToJsonString.toPrettyJson(response));

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(header().string("ETag", "\"2\""))
            .andExpect(jsonPath("$.draft_account_id").value(draftAccountId))
            .andExpect(jsonPath("$.business_unit_id").value(65))
            .andExpect(jsonPath("$.account_status").value("Published"))
            .andExpect(jsonPath("$.validated_by").value("USER01"))
            .andExpect(jsonPath("$.validated_by_name").value("normal@users.com"))
            .andExpect(jsonPath("$.timeline_data[0].username").value("johndoe456"));

        jsonSchemaValidationService.validateOrError(response, GET_DRAFT_ACCOUNT_RESPONSE);
    }

    @Test
    @DisplayName("Update draft account - If-Match Conflict [@PO-2117]")
    void testUpdateDraftAccount_conflict() throws Exception {
        Long draftAccountId = 6L;
        when(userStateService.checkForAuthorisedUser(any()))
            .thenReturn(permissionUser((short) 65, FinesPermission.CHECK_VALIDATE_DRAFT_ACCOUNTS));

        ResultActions resultActions = mockMvc.perform(patch(URL_BASE + "/" + draftAccountId)
            .header("authorization", "Bearer some_value")
            .header("If-Match", "\"9999999\"")
            .contentType(MediaType.APPLICATION_JSON)
            .content(validUpdateRequestBody("65", "Publishing Pending","A")));

        String response = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testUpdateDraftAccount_success: Response body:\n{}", ToJsonString.toPrettyJson(response));

        resultActions.andExpect(status().isConflict())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.resourceType").value("uk.gov.hmcts.opal.entity.draft.DraftAccountEntity"))
            .andExpect(jsonPath("$.resourceId").value("6"))
            .andExpect(jsonPath("$.title").value("Conflict"));
    }

    @Test
    @DisplayName("Update draft account - user with no permission [@PO-973, @PO-831]")
    void testUpdateDraftAccount_trap403Response_noPermission() throws Exception {
        Long draftAccountId = 241L;
        String requestBody = "            {\n"
            + "                \"account_status\": \"PENDING\",\n"
            + "                \"validated_by\": \"BUUID1\",\n"
            + "                \"business_unit_id\": 5,\n"
            + "                \"timeline_data\": "
            + validTimelineDataJson()
            + "\n"
            + "            }";

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(noFinesPermissionUser());

        mockMvc.perform(patch(URL_BASE + "/" + draftAccountId)
                .header("authorization", "Bearer some_value")
                .header("If-Match", "0")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isForbidden());

    }

    @Test
    @DisplayName("Update draft account (id=103) - company -> no PDPL logging occurs")
    void testUpdateDraftAccount_pdpl_id103_company_noPdpl() throws Exception {
        Long draftAccountId = 103L;

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allFinesPermissionUser());

        ResultActions resultActions = mockMvc.perform(patch(URL_BASE + "/" + draftAccountId)
            .header("authorization", "Bearer some_value")
            .header("If-Match", "0")
            .contentType(MediaType.APPLICATION_JSON)
            .content(validUpdateRequestBody("65", "Publishing Pending", "B")));

        String response = resultActions.andReturn().getResponse().getContentAsString();

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(header().string("ETag", "\"2\""))
            .andExpect(jsonPath("$.draft_account_id").value(draftAccountId))
            .andExpect(jsonPath("$.business_unit_id").value(65))
            .andExpect(jsonPath("$.account_status").value("Published"))
            .andExpect(jsonPath("$.timeline_data[0].username").value("johndoe456"));

        jsonSchemaValidationService.validateOrError(response, GET_DRAFT_ACCOUNT_RESPONSE);

        verify(loggingService, times(0)).personalDataAccessLogAsync(any());

    }
}
