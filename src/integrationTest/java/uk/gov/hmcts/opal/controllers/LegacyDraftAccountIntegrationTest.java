package uk.gov.hmcts.opal.controllers;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.allFinesPermissionUser;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.service.UserStateService;

@ActiveProfiles({"integration", "legacy"})
@Slf4j(topic = "opal.LegacyDraftAccountIntegrationTest")
@Sql(
    scripts = {
        "classpath:db/deleteData/delete_from_draft_accounts.sql",
        "classpath:db/insertData/insert_into_draft_accounts.sql"
    },
    executionPhase = BEFORE_TEST_METHOD
)
@Sql(scripts = "classpath:db/deleteData/delete_from_draft_accounts.sql", executionPhase = AFTER_TEST_METHOD)
public class LegacyDraftAccountIntegrationTest  extends AbstractIntegrationTest {

    @MockitoBean
    UserStateService userStateService;

    @Test
    @Disabled("A running instance of Legacy Stub App is required to execute this test")
    @DisplayName("Publish draft account when GOB server error")
    void testPublishDraftAccountGobServerError() throws Exception {

        Long draftAccountId = 3L;
        when(userStateService.checkForAuthorisedUser(anyString())).thenReturn(allFinesPermissionUser());

        ResultActions actions = mockMvc.perform(patch("/draft-accounts/" + draftAccountId)
            .header("authorization", "Bearer some_value")
            .header("If-Match", getIfMatchForDraftAccount(draftAccountId))
            .contentType(MediaType.APPLICATION_JSON)
            .content(validUpdateRequestBody("73", "Publishing Pending","A")));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDefendantAccountsSearch: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().string(not(containsString("Result FCOST is not valid"))));
    }

    private String getIfMatchForDraftAccount(long draftAccountId) throws Exception {
        return mockMvc.perform(get("/draft-accounts/" + draftAccountId)
                .header("authorization", "Bearer some_value")
                .header("Accept", "application/json"))
            .andReturn()
            .getResponse()
            .getHeader("ETag");
    }

    private static String validUpdateRequestBody(String businessUnit, String status, String delta) {
        return "{\n"
            + "    \"account_status\": \"" + status + "\",\n"
            + "    \"validated_by\": \"BUUID1" + delta + "\",\n"
            + "    \"validated_by_name\": \"" + delta + "\",\n"
            + "    \"business_unit_id\": " + businessUnit + ",\n"
            + "    \"version\": 0,\n"
            + "    \"timeline_data\": " + validTimelineDataJson() + "\n"
            + "}";
    }

    private static String validTimelineDataJson() {
        return """
            [
                {
                    "username": "johndoe456",
                    "status": "Active",
                    "status_date": "2023-11-01",
                    "reason_text": "Account successfully activated after review."
                },
                {
                    "username": "janedoe789",
                    "status": "Pending",
                    "status_date": "2023-12-05",
                    "reason_text": "Awaiting additional documentation for verification."
                },
                {
                    "username": "mikebrown012",
                    "status": "Suspended",
                    "status_date": "2023-10-15",
                    "reason_text": "Violation of terms of service."
                }
            ]""";
    }

}
