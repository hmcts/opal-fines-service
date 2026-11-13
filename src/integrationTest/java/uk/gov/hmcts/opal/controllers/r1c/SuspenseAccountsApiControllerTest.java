package uk.gov.hmcts.opal.controllers.r1c;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.authorisation.model.FinesPermission.VIEW_SUSPENSE_ITEMS;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.generated.model.BusinessUnitSummaryLiteCommon;
import uk.gov.hmcts.opal.generated.model.SuspenseItemSummaryCommonStrict;
import uk.gov.hmcts.opal.service.SuspenseAccountsSummaryService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@DisplayName("Suspense item summary database integration tests")
@Sql(scripts = "classpath:db/insertData/insert_into_suspense_item_summary.sql", executionPhase = BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:db/deleteData/delete_from_suspense_item_summary.sql", executionPhase = AFTER_TEST_CLASS)
@TestPropertySource(properties = "launchdarkly.default-flag-values.release-1c-suspense=true")
class SuspenseAccountsApiControllerTest extends AbstractIntegrationTest {

    private static final String SUMMARY_URL =
        "/suspense-accounts/{business_unit_id}/suspense-items/{suspense_item_number}/summary";
    private static final String SUMMARY_URL
        = "/suspense-accounts/{business_unit_id}/suspense-items/{suspense_item_number}/summary";
    private static final String AUTH_HEADER = "Bearer some_value";

    @ParameterizedTest
    @EnumSource(value = FinesPermission.class, names = { "VIEW_SUSPENSE_ITEMS", "MANAGE_SUSPENSE_ITEMS" })
    @JiraStory("PO-9026")
    @JiraEpic("PO-9026")
    @DisplayName("Either suspense permission allows retrieving a stored item and its transaction balance")
    void getSummary_returnsStoredItem(FinesPermission permission) throws Exception {
        getSummary((short) 29101, 100000L, permission)
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.business_unit_details.business_unit_id").value(29101))
            .andExpect(jsonPath("$.business_unit_details.business_unit_name").value("Suspense Summary A"))
            .andExpect(jsonPath("$.suspense_item_id").value(99000000979601L))
            .andExpect(jsonPath("$.suspense_item_number").value(100000))
            .andExpect(jsonPath("$.created_date").value("2026-11-03T10:15:30"))
            .andExpect(jsonPath("$.suspense_item_type").value("CF"))
            .andExpect(jsonPath("$.balance").value("75.25"));
    }
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
    @DisplayName("The same item number in another business unit returns that unit's item")
    void getSummary_distinguishesBusinessUnits() throws Exception {
        getSummary((short) 29102, 100000L, VIEW_SUSPENSE_ITEMS)
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.business_unit_details.business_unit_id").value(29102))
            .andExpect(jsonPath("$.business_unit_details.business_unit_name").value("Suspense Summary B"))
            .andExpect(jsonPath("$.suspense_item_id").value(99000000979602L))
            .andExpect(jsonPath("$.suspense_item_number").value(100000))
            .andExpect(jsonPath("$.balance").value("0.0"));
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
    @Test
    @JiraStory("PO-9026")
    @JiraEpic("PO-9026")
    @DisplayName("BIGINT item numbers are preserved and an item without an index entry remains visible")
    void getSummary_preservesLargeItemNumberWithoutIndex() throws Exception {
        getSummary((short) 29101, 3000000000L, VIEW_SUSPENSE_ITEMS)
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.suspense_item_id").value(99000000979603L))
            .andExpect(jsonPath("$.suspense_item_number").value(3000000000L))
            .andExpect(jsonPath("$.balance").value("0.0"));
    }

    @ParameterizedTest
    @CsvSource({ "29101, 999999", "29102, 3000000000", "29103, 100000" })
    @JiraStory("PO-9026")
    @JiraEpic("PO-9026")
    @DisplayName("Missing or mismatched business-unit and item values return not found")
    void getSummary_rejectsMissingOrMismatchedValues(short businessUnitId, long itemNumber) throws Exception {
        getSummary(businessUnitId, itemNumber, VIEW_SUSPENSE_ITEMS)
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    @JiraStory("PO-9026")
    @JiraEpic("PO-9026")
    @DisplayName("A user without a suspense permission cannot retrieve an existing item")
    void getSummary_rejectsMissingPermission() throws Exception {
        getSummary((short) 29101, 100000L)
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    private ResultActions getSummary(short businessUnitId, long itemNumber, FinesPermission... permissions)
        throws Exception {

        userStateStub.setupWithNoPermissions();
        userStateStub.addPermissions(businessUnitId, permissions);
        return mockMvc.perform(get(SUMMARY_URL, businessUnitId, itemNumber)
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken()));
    }

}
