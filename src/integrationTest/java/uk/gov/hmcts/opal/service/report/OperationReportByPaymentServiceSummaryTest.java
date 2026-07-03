package uk.gov.hmcts.opal.service.report;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;
import static uk.gov.hmcts.opal.testdata.OperationReportByPaymentFiltersIntegrationTestData.SUMMARY_SINCE_DATE_JSON;
import static uk.gov.hmcts.opal.testdata.OperationReportByPaymentFiltersIntegrationTestData.SUMMARY_SINCE_LAST_ENFORCEMENT_PAYMENT_NOT_MADE_JSON;
import static uk.gov.hmcts.opal.testdata.OperationReportByPaymentFiltersIntegrationTestData.SUMMARY_WITH_REGF_PAYMENT_MADE_JSON;
import static uk.gov.hmcts.opal.testdata.OperationReportByPaymentFiltersIntegrationTestData.reportWithFilters;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.dto.report.operation.SummaryOperationReportRowDto;
import uk.gov.hmcts.opal.dto.report.operation.SummaryReportTotalsRowDto;
import uk.gov.hmcts.opal.service.report.operation.OperationByEnforcementSummaryReport;
import uk.gov.hmcts.opal.service.report.operation.OperationReportByPaymentService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@Sql(scripts = "classpath:db/insertData/insert_into_enforcements.sql", executionPhase = BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:db/deleteData/delete_from_enforcements.sql", executionPhase = AFTER_TEST_CLASS)
@Slf4j(topic = "opal.OperationReportByPaymentServiceSummaryTest")
@DisplayName("OperationReportByPaymentServiceSummaryTest")
class OperationReportByPaymentServiceSummaryTest extends AbstractIntegrationTest {

    @Autowired
    private OperationReportByPaymentService service;

    @Nested
    @DisplayName("GenerateReportData")
    class GenerateReportData {

        @JiraStory("PO-2285")
        @JiraEpic("PO-2248")
        @Test
        void whenSummarySinceDate_thenReturnsSortedResultsAndTotals_happyPath() {
            OperationByEnforcementSummaryReport result =
                (OperationByEnforcementSummaryReport) service.generateReportData(
                    reportWithFilters(SUMMARY_SINCE_DATE_JSON));

            List<SummaryOperationReportRowDto> rows = result.getEnforcementReport().getReportSummaryRows();
            Assertions.assertThat(rows)
                .extracting(SummaryOperationReportRowDto::getAccountNo)
                .isSorted();

            SummaryReportTotalsRowDto totals = result.getEnforcementReport().getTotals();
            SummaryReportTotalsRowDto expectedTotals = expectedSummaryTotals(rows);

            assertAll(
                () -> assertThat(totals.getAccountsReported()).isEqualTo(rows.size()),
                () -> assertThat(totals.getTotalBalance()).isEqualByComparingTo(expectedTotals.getTotalBalance()),
                () -> assertThat(totals.getTotalImposed()).isEqualByComparingTo(expectedTotals.getTotalImposed()),
                () -> assertThat(totals.getTotalPaid()).isEqualByComparingTo(expectedTotals.getTotalPaid()),
                () -> verifySummaryMetadata(result, rows)
            );
        }

        @JiraStory("PO-2285")
        @JiraEpic("PO-2248")
        @Test
        void whenSummaryWithRegfAndPaymentMade_thenReturnsExpectedResults_happyPath() {
            OperationByEnforcementSummaryReport result =
                (OperationByEnforcementSummaryReport) service.generateReportData(
                    reportWithFilters(SUMMARY_WITH_REGF_PAYMENT_MADE_JSON));

            assertAll(
                () -> Assertions.assertThat(result.getEnforcementReport().getReportSummaryRows())
                    .extracting(SummaryOperationReportRowDto::getAccountNo)
                    .contains("177A")
                    .doesNotContain("noPaymentsAfterEnf")
                    .doesNotContain("ConsolidatedAcc")
            );
        }

        @JiraStory("PO-2285")
        @JiraEpic("PO-2248")
        @Test
        void whenSummarySinceLastEnforcementAndPaymentNotMade_thenReturnsExpectedResults_happyPath() {
            OperationByEnforcementSummaryReport result =
                (OperationByEnforcementSummaryReport) service.generateReportData(
                    reportWithFilters(SUMMARY_SINCE_LAST_ENFORCEMENT_PAYMENT_NOT_MADE_JSON));

            assertAll(
                () -> Assertions.assertThat(result.getEnforcementReport().getReportSummaryRows())
                    .extracting(SummaryOperationReportRowDto::getAccountNo)
                    .doesNotContain("177A")
                    .contains("ConsolidatedAcc")
                    .contains("noPaymentsAfterEnf")
            );
        }
    }

    private static void verifySummaryMetadata(
        OperationByEnforcementSummaryReport result,
        List<SummaryOperationReportRowDto> rows) {
        ReportMetaData reportMetadata = result.getReportMetaData();
        long numberOfRecords = result.getNumberOfRecords();
        assertAll(
            () -> assertThat(numberOfRecords).isEqualTo(rows.size()),
            () -> assertThat((long) reportMetadata.getPdpoPartyIds().size()).isGreaterThanOrEqualTo(numberOfRecords),
            () -> Assertions.assertThat(reportMetadata.getPdpoPartyIds()).doesNotHaveDuplicates()
        );
    }

    private static SummaryReportTotalsRowDto expectedSummaryTotals(List<SummaryOperationReportRowDto> rows) {
        BigDecimal expectedTotalBalance = rows.stream()
            .map(SummaryOperationReportRowDto::getBalance)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal expectedTotalImposed = rows.stream()
            .map(SummaryOperationReportRowDto::getAmountImposed)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal expectedTotalPaid = rows.stream()
            .map(SummaryOperationReportRowDto::getAmountPaid)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return SummaryReportTotalsRowDto.builder()
            .accountsReported(rows.size())
            .totalBalance(expectedTotalBalance)
            .totalImposed(expectedTotalImposed)
            .totalPaid(expectedTotalPaid)
            .build();
    }
}
