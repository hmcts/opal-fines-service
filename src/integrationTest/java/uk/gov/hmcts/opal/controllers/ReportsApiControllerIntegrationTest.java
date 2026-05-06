package uk.gov.hmcts.opal.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.allPermissionsUser;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.common.launchdarkly.service.FeatureToggleApi;
import uk.gov.hmcts.opal.generated.model.ReportReports;
import uk.gov.hmcts.opal.service.UserStateService;

class ReportsApiControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String URL_BASE = "/reports";

    @MockitoBean
    UserStateService userStateService;

    @MockitoBean
    FeatureToggleApi featureToggleApi;

    @BeforeEach
    void setUp() {
        when(featureToggleApi.isFeatureEnabled(anyString(), anyBoolean())).thenReturn(true);
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());
    }


    @Nested
    class GetReportByIdSuccessCases {

        @ParameterizedTest
        @MethodSource("reportCases")
        void getReportById_assertAllFields(String reportId, String expectedTitle) throws Exception {
            String body = mockMvc.perform(get(URL_BASE + "/" + reportId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

            assertCommonReportFields(objectMapper.readValue(body, ReportReports.class), reportId, expectedTitle);
        }

        static Stream<Arguments> reportCases() {
            return Stream.of(
                Arguments.of("operational_report_enforcement", "Operational report (by enforcement)"),
                Arguments.of("operational_report_payment",     "Operational report (by payment)")
            );
        }

        private void assertCommonReportFields(ReportReports report, String expectedId, String expectedTitle) {
            assertAll(
                () -> assertThat(report.getReportId()).isEqualTo(expectedId),
                () -> assertThat(report.getReportTitle()).isEqualTo(expectedTitle),
                () -> assertThat(report.getReportGroup()).isEqualTo("Operational Reports"),
                () -> assertThat(report.getSupportedFileTypes()).containsExactlyInAnyOrder(
                    ReportReports.SupportedFileTypesEnum.CSV, ReportReports.SupportedFileTypesEnum.PDF),
                () -> assertThat(report.getAuditedReport()).isFalse(),
                () -> assertThat(report.getReportParameters()).isNull(),
                () -> assertThat(report.getSupportsMultipleBusinessUnits()).isFalse(),
                () -> assertThat(report.getIsBespokeJourney()).isFalse(),
                () -> assertThat(report.getShownAsWorklist()).isFalse(),
                () -> assertThat(report.getRetentionPeriod()).isEqualTo("P14D"),
                () -> assertThat(report.getPermission()).isNull(),
                () -> assertThat(report.getCanManuallyCreate()).isTrue()
            );
        }
    }

    @Nested
    class GetReportByIdErrorCases {

        @Test
        void getReportById_whenFeatureDisabled_returns405() throws Exception {
            when(featureToggleApi.isFeatureEnabled(anyString(), anyBoolean())).thenReturn(false);

            mockMvc.perform(get(URL_BASE + "/operational_report_enforcement"))
                .andExpect(status().isMethodNotAllowed());
        }

        @Test
        void getReportById_whenReportDoesNotExist_returns404() throws Exception {

            mockMvc.perform(get(URL_BASE + "/non_existent_report"))
                .andExpect(status().isNotFound());
        }
    }
}
