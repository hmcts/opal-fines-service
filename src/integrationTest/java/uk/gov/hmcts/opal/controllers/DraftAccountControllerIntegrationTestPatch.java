package uk.gov.hmcts.opal.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.permissionUser;

import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.common.user.authorisation.model.Permission;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.ToJsonString;

@Slf4j(topic = "opal.DraftAccountControllerIntegrationTest00")
@DisplayName("DraftAccountController Integration Tests")
class DraftAccountControllerIntegrationTestPatch extends CommonDraftAccountControllerIntegrationTest {

    @Test
    @DisplayName("Patch draft account - user with CHECK_VALIDATE permission should succeed [@PO-1820]")
    void testPatchDraftAccount_withCheckValidatePermission_shouldSucceed() throws Exception {
        Long draftAccountId = 7L; // not touched by any other PATCH/PUT test
        UserState user = permissionUser((short)78, FinesPermission.CHECK_VALIDATE_DRAFT_ACCOUNTS);
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(user);

        ResultActions resultActions = mockMvc.perform(patch(URL_BASE + "/" + draftAccountId)
            .header("authorization", "Bearer some_value")
            .header("If-Match", "0")
            .contentType(MediaType.APPLICATION_JSON)
            .content(validUpdateRequestBody("78","Rejected","A")));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testPatchDraftAccount_withCheckValidatePermission_shouldSucceed: Response body:\n"
            + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(header().string("ETag", "\"1\""))
            .andExpect(jsonPath("$.draft_account_id").value(draftAccountId))
            .andExpect(jsonPath("$.account_status").value("Rejected"))
            .andExpect(jsonPath("$.timeline_data[0].username").value("johndoe456"));
    }

    @Test
    @DisplayName("Patch draft account - user with Publish Pending permission should succeed [@PO-991]")
    void testPatchDraftAccount_withPublishPending_shouldSucceed() throws Exception {
        Long draftAccountId = 9L; // not touched by any other PATCH/PUT test
        UserState user = permissionUser((short)65, FinesPermission.CHECK_VALIDATE_DRAFT_ACCOUNTS);
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(user);

        ResultActions resultActions = mockMvc.perform(patch(URL_BASE + "/" + draftAccountId)
            .header("authorization", "Bearer some_value")
            .header("If-Match", "0")
            .contentType(MediaType.APPLICATION_JSON)
            .content(validUpdateRequestBody("65", "Publishing Pending","D")));

        String response = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testPatchDraftAccount_withPublishPending_shouldSucceed: PATCH Response body:\n{}",
            ToJsonString.toPrettyJson(response));

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(header().string("ETag", "\"2\""))
            .andExpect(jsonPath("$.draft_account_id").value(draftAccountId))
            .andExpect(jsonPath("$.account_status").value("Published"))
            .andExpect(jsonPath("$.timeline_data[0].username").value("johndoe456"));
    }

    @Test
    @DisplayName("Patch draft account - submitter cannot validate their own submission")
    void testPatchDraftAccount_submitterCannotValidate_returns403() throws Exception {
        Long draftAccountId = 7L; // submitted_by = user_003 in seed data

        BusinessUnitUser buUser = BusinessUnitUser.builder()
            .businessUnitUserId("user_003")
            .businessUnitId((short)78)
            .permissions(Set.of(Permission.builder()
                                .permissionId(FinesPermission.CHECK_VALIDATE_DRAFT_ACCOUNTS.getId())
                                .permissionName(FinesPermission.CHECK_VALIDATE_DRAFT_ACCOUNTS.getDescription())
                                .build()))
            .build();
        UserState userState = UserState.builder()
            .userId(1L)
            .userName("normal@users.com")
            .businessUnitUser(Set.of(buUser))
            .build();

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(userState);

        ResultActions resultActions = mockMvc.perform(patch(URL_BASE + "/" + draftAccountId)
            .header("authorization", "Bearer some_value")
            .header("If-Match", "0")
            .contentType(MediaType.APPLICATION_JSON)
            .content(validUpdateRequestBody("78", "Publishing Pending","A")));

        resultActions.andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/submitter-cannot-validate"))
            .andExpect(jsonPath("$.title").value("Submitter cannot validate"))
            .andExpect(jsonPath("$.detail")
                           .value("A single user cannot submit and validate the same Draft Account"));
    }

    @Test
    @DisplayName("Patch draft account - user with CREATE_MANAGE permission should be forbidden [@PO-1820]")
    void testPatchDraftAccount_withCreateManagePermission_shouldFail403() throws Exception {
        Long draftAccountId = 6L;
        UserState user = permissionUser((short)78, FinesPermission.CREATE_MANAGE_DRAFT_ACCOUNTS);
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(user);

        ResultActions resultActions = mockMvc.perform(patch(URL_BASE + "/" + draftAccountId)
            .header("authorization", "Bearer some_value")
            .header("If-Match", "0")
            .contentType(MediaType.APPLICATION_JSON)
            .content(validUpdateRequestBody("78", "Publishing Pending","PO1820")));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testPatchDraftAccount_withCreateManagePermission_shouldFail403: Response body:\n"
            + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Forbidden"))
            .andExpect(jsonPath("$.status").value(403));
    }
}
