package uk.gov.hmcts.opal.service.report.mapper.csv;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.opal.entity.InterfaceFileSourceEnum;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.service.report.CashTillDestinationType;
import uk.gov.hmcts.opal.service.report.CashTillPaymentMethod;
import uk.gov.hmcts.opal.service.report.CashTillReportData;
import uk.gov.hmcts.opal.service.report.CashTillReportRow;

class CashTillReportCSVMapperTest {

    private static final String CSV_REPORT_HEADER =
        "HEADER1,report_type,till_number,source,report_amount,cashier,report_created,time_created,business_unit,"
            + "no_of_records,created_by";
    private static final String CSV_DETAIL_HEADER =
        "HEADER2,date,transaction_type,details,method_of_payment,transaction_amount";
    private static final String CSV_SUMMARY_HEADER =
        "HEADER3,no_of_records,total_amount";

    private static final String NEWER_CSV_ROW =
        "DETAIL,02/05/2026,Fines Account,*ACC123 - Auto cash input,Notes and coins,12.30";
    private static final String OLDER_CSV_ROW = "DETAIL,01/05/2026,Suspense Account,MISC123,Postal order,7.50";
    private static final String SUMMARY_ROW = "SUMMARY,2,19.80";

    private final CashTillReportCSVMapper mapper = new CashTillReportCSVMapper();

    @Test
    void getReportDataType_returnsCashTillReportDataClass() {
        assertThat(mapper.getReportDataType()).isEqualTo(CashTillReportData.class);
    }

    @Test
    void reportToCSVString_whenValidData_returnsF061Csv() {
        CashTillReportData reportData = allocatedReportData(List.of(olderRow(), newerRow()));

        String csv = mapper.reportToCSVString(typicalReportInstance(), reportData);

        assertThat(csv).isEqualTo(typicalCsvWithRows(List.of(NEWER_CSV_ROW, OLDER_CSV_ROW), "19.80", SUMMARY_ROW));
    }

    @Test
    void reportToCSVString_whenRowsAreEmpty_returnsHeaderOnly() {
        CashTillReportData reportData = reportDataWithRows(List.of());

        String csv = mapper.reportToCSVString(typicalReportInstance(), reportData);

        assertThat(csv).isEqualTo(typicalCsvWithNoRows());
    }

    @Test
    void reportToCSVString_doesNotMutateInputRows() {
        CashTillReportRow olderRow = olderRow();
        CashTillReportRow newerRow = newerRow();
        List<CashTillReportRow> rows = new ArrayList<>(List.of(olderRow, newerRow));
        CashTillReportData reportData = allocatedReportData(rows);

        mapper.reportToCSVString(typicalReportInstance(), reportData);

        assertThat(reportData.getRows()).containsExactly(olderRow, newerRow);
    }

    @Test
    void reportToCSVString_whenReportDataIsNull_throwsException() {
        assertThatThrownBy(() -> mapper.reportToCSVString(typicalReportInstance(),null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Cash Till report data is required");
    }

    @Test
    void reportToCSVString_whenRowsAreNull_throwsException() {
        CashTillReportData reportData = reportDataWithRows(null);

        assertThatThrownBy(() -> mapper.reportToCSVString(typicalReportInstance(), reportData))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Cash Till report rows are required");
    }

    @Test
    void reportToCSVString_whenRowIsNull_throwsException() {
        List<CashTillReportRow> rows = new ArrayList<>();
        rows.add(null);
        CashTillReportData reportData = reportDataWithRows(rows);

        assertThatThrownBy(() -> mapper.reportToCSVString(typicalReportInstance(), reportData))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Cash Till report row 1 is required");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("missingFieldCases")
    void reportToCSVString_whenMandatoryFieldIsMissing_throwsException(String description,
        Consumer<CashTillReportRow> change, String expectedMessage) {
        CashTillReportRow row = validRow();
        change.accept(row);

        assertThatThrownBy(() -> mapper.reportToCSVString(typicalReportInstance(), reportData(row)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage(expectedMessage);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidMoneyCases")
    void reportToCSVString_moneyScaleTooHigh_throwsException(String description, Consumer<CashTillReportRow> change,
        String expectedMessage) {
        CashTillReportRow row = validRow();
        change.accept(row);

        assertThatThrownBy(() -> mapper.reportToCSVString(typicalReportInstance(), reportData(row)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage(expectedMessage);
    }

    private static Stream<Arguments> missingFieldCases() {
        return Stream.of(
            missingFieldCase("date", row -> row.setPaymentDateTime(null), "date"),
            missingFieldCase("details", row -> row.setDetails(null), "details"),
            missingFieldCase("transaction type", row -> row.setDestinationType(null), "transaction_type"),
            missingFieldCase("payment method", row -> row.setPaymentMethod(null), "method_of_payment"),
            missingFieldCase("transaction amount", row -> row.setAmount(null), "transaction_amount")
        );
    }
    private static Stream<Arguments> invalidMoneyCases() {
        return Stream.of(
            invalidMoneyCase("amount", row -> row.setAmount(money("1.234")), "transaction_amount"));
    }

    private static Arguments missingFieldCase(String description, Consumer<CashTillReportRow> change,
        String fieldName) {
        return Arguments.of(description, change, fieldName + " is required at Cash Till report row 1");
    }

    private static Arguments invalidMoneyCase(String description, Consumer<CashTillReportRow> change,
        String fieldName) {
        String message = fieldName + " must not have more than two decimal places at Cash Till report row 1";
        return Arguments.of(description, change, message);
    }

    private static CashTillReportData reportData(CashTillReportRow row) {
        return reportDataWithRows(List.of(row));
    }

    private static CashTillReportData reportDataWithRows(List<CashTillReportRow> rows) {
        return CashTillReportData.builder()
            .businessUnit("Central Unit")
            .cashier("Alex")
            .cashTillNumber("22")
            .source(InterfaceFileSourceEnum.BARCLAYCARD)
            .allocatedReport(true)
            .totalAmount(rows == null ? BigDecimal.ZERO :
                rows.stream().map(row -> row == null || row.getAmount() == null
                                         ? BigDecimal.ZERO : row.getAmount()).reduce(BigDecimal.ZERO, BigDecimal::add))
            .rows(rows)
            .build();
    }

    private static CashTillReportData allocatedReportData(List<CashTillReportRow> rows) {
        return CashTillReportData.builder()
            .businessUnit("Central Unit")
            .cashier("Alex")
            .cashTillNumber("22")
            .source(InterfaceFileSourceEnum.BARCLAYCARD)
            .totalAmount(rows.stream().map(CashTillReportRow::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add))
            .allocatedReport(true)
            .rows(rows)
            .build();
    }

    private static CashTillReportRow newerRow() {
        return CashTillReportRow.builder()
            .paymentDateTime(LocalDateTime.of(2026, 5, 2, 14, 5))
            .destinationType(CashTillDestinationType.FA)
            .details("ACC123")
            .autoPayment(true)
            .paymentMethod(CashTillPaymentMethod.NC)
            .amount(money("12.3"))
            .receipt(true)
            .balance(money("12.30"))
            .allocated(true)
            .build();
    }

    private static CashTillReportRow olderRow() {
        return CashTillReportRow.builder()
            .paymentDateTime(LocalDateTime.of(2026, 5, 1, 9, 30))
            .destinationType(CashTillDestinationType.SA)
            .details("MISC123")
            .autoPayment(false)
            .paymentMethod(CashTillPaymentMethod.PO)
            .amount(money("7.50"))
            .receipt(false)
            .balance(money("99.00"))
            .allocated(false)
            .build();
    }

    private static CashTillReportRow validRow() {
        return CashTillReportRow.builder()
            .paymentDateTime(LocalDateTime.of(2026, 5, 3, 10, 15))
            .destinationType(CashTillDestinationType.FA)
            .details("ACC999")
            .autoPayment(false)
            .paymentMethod(CashTillPaymentMethod.CQ)
            .amount(money("1.20"))
            .receipt(true)
            .balance(money("1.20"))
            .allocated(false)
            .build();
    }

    private static BigDecimal money(String value) {
        return new BigDecimal(value);
    }

    private static String typicalCsvWithNoRows() {
        return typicalCsvWithRows(List.of(), "0.00", "SUMMARY,0,0");
    }

    private static String typicalCsvWithRows(List<String> expectedRows, String totalAmount, String summary) {
        StringBuilder csv = new StringBuilder(CSV_REPORT_HEADER).append("\n");
        csv.append(typicalCsvReportRow(totalAmount, String.valueOf(expectedRows.size())));
        csv.append("\n\n");
        csv.append(CSV_DETAIL_HEADER).append("\n");
        expectedRows.forEach(r -> csv.append(r).append("\n"));
        csv.append("\n");
        csv.append(CSV_SUMMARY_HEADER).append("\n");
        csv.append(summary);
        return csv.toString();
    }

    private static String typicalCsvReportRow(String totalAmount, String numRecords) {
        return "REPORT,Allocated,22,BARCLAYCARD," + totalAmount + ",Alex,30/10/2026,01:02,Central Unit,"
            + numRecords + ",John Smith";
    }

    private static ReportInstanceEntity typicalReportInstance() {
        return ReportInstanceEntity.builder()
            .businessUnit(List.of((short)11))
            .createdTimestamp(LocalDateTime.of(2026, 10, 30, 1, 2, 3))
            .requestedByName("John Smith")
            .build();
    }
}
