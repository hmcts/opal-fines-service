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
import uk.gov.hmcts.opal.service.report.CashTillDestinationType;
import uk.gov.hmcts.opal.service.report.CashTillPaymentMethod;
import uk.gov.hmcts.opal.service.report.CashTillReportData;
import uk.gov.hmcts.opal.service.report.CashTillReportRow;

class CashTillReportCSVMapperTest {

    private static final String CSV_HEADER =
        "Business Unit,Cash Till Number,Cashier,Date,Type,Details,Payment Type,Amount,Receipt,Balance";
    private static final String NEWER_CSV_ROW =
        "\"North, Court\",9,\"Cashier \"\"A\"\"\",02/05/2026,FA,*ACC123 - Auto cash input,NC,12.30,R,12.30";
    private static final String OLDER_CSV_ROW = "Westshire,17,Pedro,01/05/2026,SA,MISC123,PO,7.50,,99.00";

    private final CashTillReportCSVMapper mapper = new CashTillReportCSVMapper();

    @Test
    void getReportDataType_returnsCashTillReportDataClass() {
        assertThat(mapper.getReportDataType()).isEqualTo(CashTillReportData.class);
    }

    @Test
    void reportToCSVString_whenValidData_returnsF061Csv() {
        CashTillReportData reportData = allocatedReportData(List.of(olderRow(), newerRow()));

        String csv = mapper.reportToCSVString(reportData);

        assertThat(csv).isEqualTo(String.join("\n", CSV_HEADER, NEWER_CSV_ROW, OLDER_CSV_ROW, ""));
    }

    @Test
    void reportToCSVString_whenRowsAreEmpty_returnsHeaderOnly() {
        CashTillReportData reportData = reportDataWithRows(List.of());

        String csv = mapper.reportToCSVString(reportData);

        assertThat(csv).isEqualTo(CSV_HEADER + "\n");
    }

    @Test
    void reportToCSVString_doesNotMutateInputRows() {
        CashTillReportRow olderRow = olderRow();
        CashTillReportRow newerRow = newerRow();
        List<CashTillReportRow> rows = new ArrayList<>(List.of(olderRow, newerRow));
        CashTillReportData reportData = allocatedReportData(rows);

        mapper.reportToCSVString(reportData);

        assertThat(reportData.getRows()).containsExactly(olderRow, newerRow);
    }

    @Test
    void reportToCSVString_whenReportDataIsNull_throwsException() {
        assertThatThrownBy(() -> mapper.reportToCSVString(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Cash Till report data is required");
    }

    @Test
    void reportToCSVString_whenRowsAreNull_throwsException() {
        CashTillReportData reportData = reportDataWithRows(null);

        assertThatThrownBy(() -> mapper.reportToCSVString(reportData))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Cash Till report rows are required");
    }

    @Test
    void reportToCSVString_whenRowIsNull_throwsException() {
        List<CashTillReportRow> rows = new ArrayList<>();
        rows.add(null);
        CashTillReportData reportData = reportDataWithRows(rows);

        assertThatThrownBy(() -> mapper.reportToCSVString(reportData))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Cash Till report row 1 is required");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("missingFieldCases")
    void reportToCSVString_whenMandatoryFieldIsMissing_throwsException(String description,
        Consumer<CashTillReportRow> change, String expectedMessage) {
        CashTillReportRow row = validRow();
        change.accept(row);

        assertThatThrownBy(() -> mapper.reportToCSVString(reportData(row)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage(expectedMessage);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidMoneyCases")
    void reportToCSVString_moneyScaleTooHigh_throwsException(String description, Consumer<CashTillReportRow> change,
        String expectedMessage) {
        CashTillReportRow row = validRow();
        change.accept(row);

        assertThatThrownBy(() -> mapper.reportToCSVString(reportData(row)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage(expectedMessage);
    }

    private static Stream<Arguments> missingFieldCases() {
        return Stream.of(
            missingFieldCase("business unit", row -> row.setBusinessUnit(null), "Business Unit"),
            missingFieldCase("cash till number", row -> row.setCashTillNumber(""), "Cash Till Number"),
            missingFieldCase("cashier", row -> row.setCashier(" "), "Cashier"),
            missingFieldCase("date", row -> row.setPaymentDateTime(null), "Date"),
            missingFieldCase("details", row -> row.setDetails(null), "Details"),
            missingFieldCase("auto payment", row -> row.setAutoPayment(null), "auto_payment"),
            missingFieldCase("receipt", row -> row.setReceipt(null), "Receipt"),
            missingFieldCase("amount", row -> row.setAmount(null), "Amount"),
            missingFieldCase("balance", row -> row.setBalance(null), "Balance"));
    }

    private static Stream<Arguments> invalidMoneyCases() {
        return Stream.of(
            invalidMoneyCase("amount", row -> row.setAmount(money("1.234")), "Amount"),
            invalidMoneyCase("balance", row -> row.setBalance(money("2.345")), "Balance"));
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
            .rows(rows)
            .build();
    }

    private static CashTillReportData allocatedReportData(List<CashTillReportRow> rows) {
        return CashTillReportData.builder()
            .allocatedReport(true)
            .rows(rows)
            .build();
    }

    private static CashTillReportRow newerRow() {
        return CashTillReportRow.builder()
            .businessUnit("North, Court")
            .cashTillNumber("9")
            .cashier("Cashier \"A\"")
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
            .businessUnit("Westshire")
            .cashTillNumber("17")
            .cashier("Pedro")
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
            .businessUnit("Central")
            .cashTillNumber("22")
            .cashier("Alex")
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
}
