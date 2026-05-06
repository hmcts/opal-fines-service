package uk.gov.hmcts.opal.controllers;

import static org.mockito.Mockito.lenient;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.SchemaPaths;
import uk.gov.hmcts.opal.common.logging.SecurityEventLoggingService;
import uk.gov.hmcts.opal.common.user.authorisation.client.service.UserStateClientService;
import uk.gov.hmcts.opal.logging.integration.service.LoggingService;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;

@ActiveProfiles({"integration"})
@Sql(
    scripts = {
        "classpath:db/deleteData/delete_from_draft_accounts.sql",
        "classpath:db/insertData/insert_into_draft_accounts.sql"
    },
    executionPhase = BEFORE_TEST_CLASS
)
@Sql(scripts = "classpath:db/deleteData/delete_from_draft_accounts.sql", executionPhase = AFTER_TEST_CLASS)
@DisplayName("CommonDraftAccountControllerIntegrationTest")
class CommonDraftAccountControllerIntegrationTest extends AbstractIntegrationTest {

    static final Short BU_ID = (short)73;
    static final String URL_BASE = "/draft-accounts";
    static final String GET_DRAFT_ACCOUNT_RESPONSE = SchemaPaths.DRAFT_ACCOUNT + "/getDraftAccountResponse.json";
    static final String GET_DRAFT_ACCOUNTS_RESPONSE = SchemaPaths.DRAFT_ACCOUNT + "/getDraftAccountsResponse.json";
    static final LocalDate TIMELINE_STATUS_DATE = LocalDate.of(2026, 4, 22);
    private static final Clock FIXED_CLOCK = Clock.fixed(
        TIMELINE_STATUS_DATE.atStartOfDay(ZoneOffset.UTC).toInstant(),
        ZoneOffset.UTC
    );

    @MockitoBean
    UserStateService userStateService;

    @MockitoBean
    UserStateClientService userStateClientService;

    @MockitoBean
    LoggingService loggingService;

    @MockitoBean
    SecurityEventLoggingService securityEventLoggingService;

    @MockitoSpyBean
    JsonSchemaValidationService jsonSchemaValidationService;

    @MockitoBean
    Clock clock;

    @BeforeEach
    void setupClock() {
        lenient().when(clock.instant()).thenReturn(FIXED_CLOCK.instant());
        lenient().when(clock.getZone()).thenReturn(FIXED_CLOCK.getZone());
    }

    protected static String validUpdateRequestBody(String businessUnit, String status, String delta) {
        return """
            {
              "account_status": "%2$s",
              "validated_by": "BUUID1%3$s",
              "validated_by_name": "%3$s",
              "business_unit_id": %1$s,
              "reason_text": "Reason %3$s",
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

    protected String getIfMatchForDraftAccount(long draftAccountId) throws Exception {
        return mockMvc.perform(get(URL_BASE + "/" + draftAccountId)
                .header("authorization", "Bearer some_value")
                .header("Accept", "application/json"))
            .andReturn()
            .getResponse()
            .getHeader("ETag");
    }

}
