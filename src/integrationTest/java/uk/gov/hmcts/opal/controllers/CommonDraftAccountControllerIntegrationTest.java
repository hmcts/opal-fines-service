package uk.gov.hmcts.opal.controllers;

import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static uk.gov.hmcts.opal.authorisation.model.FinesPermission.CHECK_VALIDATE_DRAFT_ACCOUNTS;
import static uk.gov.hmcts.opal.authorisation.model.FinesPermission.CREATE_MANAGE_DRAFT_ACCOUNTS;
import static uk.gov.hmcts.opal.support.UserServiceStub.stubDeveloperUserWithAllPermissions;
import static uk.gov.hmcts.opal.support.UserServiceStub.stubNormalUserWithPermissions;
import static uk.gov.hmcts.opal.support.UserServiceStub.stubNormalUserWithPermissionsForBusinessUnits;

import org.junit.jupiter.api.DisplayName;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.opal.AbstractIntegrationWithSecurityTest;
import uk.gov.hmcts.opal.SchemaPaths;
import uk.gov.hmcts.opal.common.logging.SecurityEventLoggingService;
import uk.gov.hmcts.opal.logging.integration.service.LoggingService;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;

@ActiveProfiles(profiles = {"integration-with-spring-security"}, inheritProfiles = false)
@Sql(
    scripts = {
        "classpath:db/deleteData/delete_from_draft_accounts.sql",
        "classpath:db/insertData/insert_into_draft_accounts.sql"
    },
    executionPhase = BEFORE_TEST_CLASS
)
@Sql(scripts = "classpath:db/deleteData/delete_from_draft_accounts.sql", executionPhase = AFTER_TEST_CLASS)
@DisplayName("CommonDraftAccountControllerIntegrationTest")
class CommonDraftAccountControllerIntegrationTest extends AbstractIntegrationWithSecurityTest {

    static final Short BU_ID = (short)73;
    static final String URL_BASE = "/draft-accounts";
    static final String GET_DRAFT_ACCOUNT_RESPONSE = SchemaPaths.DRAFT_ACCOUNT + "/getDraftAccountResponse.json";
    static final String GET_DRAFT_ACCOUNTS_RESPONSE = SchemaPaths.DRAFT_ACCOUNT + "/getDraftAccountsResponse.json";
    private static final int[] DRAFT_TEST_BUSINESS_UNIT_IDS = {5, 65, 73, 77, 78};

    @MockitoBean
    LoggingService loggingService;

    @MockitoBean
    SecurityEventLoggingService securityEventLoggingService;

    @MockitoSpyBean
    JsonSchemaValidationService jsonSchemaValidationService;

    protected static String validUpdateRequestBody(String businessUnit, String status, String delta) {
        return """
            {
              "account_status": "%2$s",
              "validated_by": "BUUID1%3$s",
              "validated_by_name": "%3$s",
              "business_unit_id": %1$s,
              "version": 0,
              "timeline_data": %4$s
            }
            """.formatted(businessUnit, status, delta, validTimelineDataJson());
    }

    protected static String validTimelineDataJson() {
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

    protected static void authoriseNormalUserForDraftAccount(int businessUnitId) {
        stubNormalUserWithPermissions(businessUnitId, CREATE_MANAGE_DRAFT_ACCOUNTS, CHECK_VALIDATE_DRAFT_ACCOUNTS);
    }

    protected static void authoriseNormalUserForDraftAccountWithCheckValidate(int businessUnitId) {
        stubNormalUserWithPermissions(businessUnitId, CHECK_VALIDATE_DRAFT_ACCOUNTS);
    }

    protected static void authoriseNormalUserForDraftAccountWithCreateManage(int businessUnitId) {
        stubNormalUserWithPermissions(businessUnitId, CREATE_MANAGE_DRAFT_ACCOUNTS);
    }

    protected static void authoriseNormalUserForDraftAccounts(int... businessUnitIds) {
        stubNormalUserWithPermissionsForBusinessUnits(
            businessUnitIds, CREATE_MANAGE_DRAFT_ACCOUNTS, CHECK_VALIDATE_DRAFT_ACCOUNTS);
    }

    protected static void authoriseDeveloperUserForAllDraftTestBusinessUnits() {
        stubDeveloperUserWithAllPermissions(DRAFT_TEST_BUSINESS_UNIT_IDS);
    }

    protected String getIfMatchForDraftAccount(long draftAccountId) throws Exception {
        return mockMvc.perform(get(URL_BASE + "/" + draftAccountId)
                .header("authorization", "Bearer " + validToken)
                .header("Accept", "application/json"))
            .andReturn()
            .getResponse()
            .getHeader("ETag");
    }

}
