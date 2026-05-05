package uk.gov.hmcts.opal.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.allPermissionsUser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.generated.model.ReportReports;
import uk.gov.hmcts.opal.service.UserStateService;

@DisplayName("ReportsApiController Integration Test")
class ReportsApiControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String URL_BASE = "/reports";
    private static final String AUTHORIZATION_HEADER = "authorization";
    private static final String BEARER_TOKEN = "Bearer some_value";

    @MockitoBean
    UserStateService userStateService;

    @Nested
    @DisplayName("GET /reports/{id} - Success Cases")
    class GetReportByIdSuccessCases {

        @Test
        @DisplayName("Get report asserts all fields for operational_report_enforcement [@PO-2250]")
        void getReportByIdAssertAllFieldsEnforcement() throws Exception {
            when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

            String body = mockMvc.perform(
                    get(URL_BASE + "/operational_report_enforcement").header(AUTHORIZATION_HEADER, BEARER_TOKEN))
                .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn()
                .getResponse().getContentAsString();

            ReportReports report = objectMapper.readValue(body, ReportReports.class);

            assertAll(() -> assertThat(report.getReportId()).isEqualTo("operational_report_enforcement"),
                () -> assertThat(report.getReportTitle()).isEqualTo("Operational report (by enforcement)"),
                () -> assertThat(report.getReportGroup()).isEqualTo("Operational Reports"),
                () -> assertThat(report.getSupportedFileTypes()).containsExactlyInAnyOrder(
                    ReportReports.SupportedFileTypesEnum.CSV, ReportReports.SupportedFileTypesEnum.PDF),
                () -> assertThat(report.getAuditedReport()).isFalse(),
                () -> assertThat(report.getReportParameters()).isNull(),
                () -> assertThat(report.getSupportsMultipleBusinessUnits()).isFalse(),
                () -> assertThat(report.getIsBespokeJourney()).isFalse(),
                () -> assertThat(report.getShownAsWorklist()).isFalse(),
                () -> assertThat(report.getRetentionPeriod()).isEqualTo("PT0.000000014S"),
                () -> assertThat(report.getPermission()).isNull(),
                () -> assertThat(report.getCanManuallyCreate()).isTrue());
        }

        @Test
        @DisplayName("Get report asserts all fields for operational_report_payment [@PO-2250]")
        void getReportByIdAssertAllFieldsPayment() throws Exception {
            when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

            String body = mockMvc.perform(
                    get(URL_BASE + "/operational_report_payment").header(AUTHORIZATION_HEADER, BEARER_TOKEN))
                .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn()
                .getResponse().getContentAsString();

            ReportReports report = objectMapper.readValue(body, ReportReports.class);

            assertAll(() -> assertThat(report.getReportId()).isEqualTo("operational_report_payment"),
                () -> assertThat(report.getReportTitle()).isEqualTo("Operational report (by payment)"),
                () -> assertThat(report.getReportGroup()).isEqualTo("Operational Reports"),
                () -> assertThat(report.getSupportedFileTypes()).containsExactlyInAnyOrder(
                    ReportReports.SupportedFileTypesEnum.CSV, ReportReports.SupportedFileTypesEnum.PDF),
                () -> assertThat(report.getAuditedReport()).isFalse(),
                () -> assertThat(report.getReportParameters()).isNull(),
                () -> assertThat(report.getSupportsMultipleBusinessUnits()).isFalse(),
                () -> assertThat(report.getIsBespokeJourney()).isFalse(),
                () -> assertThat(report.getShownAsWorklist()).isFalse(),
                () -> assertThat(report.getRetentionPeriod()).isEqualTo("PT0.000000014S"),
                () -> assertThat(report.getPermission()).isNull(),
                () -> assertThat(report.getCanManuallyCreate()).isTrue());
        }
    }

    @Nested
    @DisplayName("GET /reports/{id} - Error Cases")
    class GetReportByIdErrorCases {

        @Test
        @DisplayName("No report returned when report does not exist [@PO-2250]")
        void getReportByIdWhenReportDoesNotExist() throws Exception {
            when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

            mockMvc.perform(get(URL_BASE + "/non_existent_report").header(AUTHORIZATION_HEADER, BEARER_TOKEN))
                .andExpect(status().isNotFound());
        }
    }
}
