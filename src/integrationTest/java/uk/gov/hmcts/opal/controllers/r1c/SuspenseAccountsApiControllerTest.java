package uk.gov.hmcts.opal.controllers.r1c;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.generated.model.BusinessUnitSummaryLiteCommon;
import uk.gov.hmcts.opal.generated.model.SuspenseItemSummaryCommonStrict;
import uk.gov.hmcts.opal.service.SuspenseAccountsSummaryService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

public class SuspenseAccountsApiControllerTest extends AbstractIntegrationTest {

    private static final String SUMMARY_URL
        = "/suspense-accounts/{business_unit_id}/suspense-items/{suspense_item_number}/summary";
    private static final String AUTH_HEADER = "Bearer some_value";

    @MockitoBean
    private SuspenseAccountsSummaryService suspenseAccountsSummaryService;

    @Test
    @JiraStory("PO-9026")
    @JiraEpic("PO-9026")
    public void test_ShouldReturnSummary() throws Exception {
        userStateStub.setupWithNoPermissions();
        userStateStub.addPermissions((short) 77, FinesPermission.VIEW_SUSPENSE_ITEMS,
            FinesPermission.MANAGE_SUSPENSE_ITEMS);

        SuspenseItemSummaryCommonStrict expected = SuspenseItemSummaryCommonStrict
            .builder()
            .suspenseItemId(1)
            .suspenseItemNumber(12345)
            .businessUnitDetails(BusinessUnitSummaryLiteCommon
                .builder()
                .businessUnitId((short) 1)
                .businessUnitName("Business Unit Name").build())
            .createdDate(LocalDateTime.parse("2026-11-03T10:15:30"))
            .balance("100.0")
            .build();

        doReturn(expected).when(suspenseAccountsSummaryService).getSuspenseAccountsSummary(any(), any());

        ResultActions resultActions = mockMvc.perform(get(SUMMARY_URL, 123, 123L)
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER));

        resultActions
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.business_unit_details.business_unit_id").value(1))
            .andExpect(jsonPath("$.business_unit_details.business_unit_name").value("Business Unit Name"))
            .andExpect(jsonPath("$.suspense_item_id").value(1))
            .andExpect(jsonPath("$.suspense_item_number").value(12345))
            .andExpect(jsonPath("$.created_date").value("2026-11-03T10:15:30"))
            .andExpect(jsonPath("$.balance").value("100.0"));
    }

    @Test
    @JiraStory("PO-9026")
    @JiraEpic("PO-9026")
    public void test_ShouldReturnNotFound() throws Exception {
        userStateStub.setupWithNoPermissions();
        userStateStub.addPermissions((short) 77, FinesPermission.VIEW_SUSPENSE_ITEMS,
            FinesPermission.MANAGE_SUSPENSE_ITEMS);

        doReturn(null).when(suspenseAccountsSummaryService).getSuspenseAccountsSummary(any(), any());

        ResultActions resultActions = mockMvc.perform(get(SUMMARY_URL, 123, 123L)
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER));

        resultActions.andExpect(status().isNotFound());
    }

}
