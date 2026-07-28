package uk.gov.hmcts.opal.service.report;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;
import static uk.gov.hmcts.opal.testdata.OperationReportByPaymentFiltersIntegrationTestData.SUMMARY_SINCE_DATE_JSON;
import static uk.gov.hmcts.opal.testdata.OperationReportByPaymentFiltersIntegrationTestData.SUMMARY_SINCE_LAST_ENFORCEMENT_PAYMENT_NOT_MADE_JSON;
import static uk.gov.hmcts.opal.testdata.OperationReportByPaymentFiltersIntegrationTestData.SUMMARY_WITH_REGF_PAYMENT_MADE_JSON;
import static uk.gov.hmcts.opal.testdata.OperationReportByPaymentFiltersIntegrationTestData.reportWithFilters;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.dto.report.operation.SummaryOperationReportRowDto;
import uk.gov.hmcts.opal.dto.report.operation.SummaryReportTotalsRowDto;
import uk.gov.hmcts.opal.service.report.operation.OperationSummaryReport;
import uk.gov.hmcts.opal.service.report.operation.PaymentReportService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@Sql(scripts = "classpath:db/insertData/insert_into_enforcements.sql", executionPhase = BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:db/deleteData/delete_from_enforcements.sql", executionPhase = AFTER_TEST_CLASS)
@Slf4j(topic = "opal.PaymentSummaryReportServiceTest")
@DisplayName("PaymentSummaryReportServiceTest")
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class PaymentSummaryReportServiceTest extends AbstractIntegrationTest {

    private final PaymentReportService service;

    @Nested
    @DisplayName("GenerateReportData")
    class GenerateReportData {

        @JiraStory("PO-2285")
        @JiraEpic("PO-2248")
        @Test
        void whenSummarySinceDate_thenReturnsSortedResultsAndTotals_happyPath() {
            OperationSummaryReport result =
                (OperationSummaryReport) service.generateReportData(
                    reportWithFilters(SUMMARY_SINCE_DATE_JSON));

            List<SummaryOperationReportRowDto> rows = result.getSummaryReport().getReportSummaryRows();
            Assertions.assertThat(rows)
                .extracting(SummaryOperationReportRowDto::getAccountNo)
                .containsExactly("177A");

            SummaryReportTotalsRowDto totals = result.getSummaryReport().getTotals();

            assertAll(
                () -> assertThat(totals.getAccountsReported()).isEqualTo(1),
                () -> assertThat(totals.getTotalBalance()).isEqualByComparingTo("-500.58"),
                () -> assertThat(totals.getTotalImposed()).isEqualByComparingTo("700.58"),
                () -> assertThat(totals.getTotalPaid()).isEqualByComparingTo("200.00"),
                () -> verifySummaryMetadata(result, rows)
            );
        }

        @JiraStory("PO-2285")
        @JiraEpic("PO-2248")
        @Test
        void summaryRegfPaymentMade_returnsExpectedRows() {
            OperationSummaryReport result =
                (OperationSummaryReport) service.generateReportData(
                    reportWithFilters(SUMMARY_WITH_REGF_PAYMENT_MADE_JSON));

            assertAll(
                () -> Assertions.assertThat(result.getSummaryReport().getReportSummaryRows())
                    .extracting(SummaryOperationReportRowDto::getAccountNo)
                    .contains("177A")
                    .doesNotContain("noPaymentsAfterEnf")
                    .doesNotContain("ConsolidatedAcc")
            );
        }

        @JiraStory("PO-2285")
        @JiraEpic("PO-2248")
        @Test
        void summarySinceLastEnforcementPaymentNotMade_returnsExpectedRows() {
            OperationSummaryReport result =
                (OperationSummaryReport) service.generateReportData(
                    reportWithFilters(SUMMARY_SINCE_LAST_ENFORCEMENT_PAYMENT_NOT_MADE_JSON));

            assertAll(
                () -> Assertions.assertThat(result.getSummaryReport().getReportSummaryRows())
                    .extracting(SummaryOperationReportRowDto::getAccountNo)
                    .doesNotContain("177A")
                    .contains("ConsolidatedAcc")
                    .contains("noPaymentsAfterEnf")
            );
        }
    }

    private static void verifySummaryMetadata(
        OperationSummaryReport result,
        List<SummaryOperationReportRowDto> rows) {
        ReportMetaData reportMetadata = result.getReportMetaData();
        long numberOfRecords = result.getNumberOfRecords();
        assertAll(
            () -> assertThat(numberOfRecords).isEqualTo(rows.size()),
            () -> assertThat((long) reportMetadata.getPdpoPartyIds().size()).isGreaterThanOrEqualTo(numberOfRecords),
            () -> Assertions.assertThat(reportMetadata.getPdpoPartyIds()).doesNotHaveDuplicates()
        );
    }
}
