package uk.gov.hmcts.opal.service.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.opal.service.report.FileType.CSV;
import static uk.gov.hmcts.opal.service.report.FileType.PDF;
import static uk.gov.hmcts.opal.service.report.ReportId.CASH_TILL;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;

class CashTillReportServiceTest {

    private static final String CSV_HEADER =
        "Business Unit,Cash Till Number,Cashier,Date,Type,Details,Payment Type,Amount,Receipt,Balance";
    private static final String NEWER_CSV_ROW =
        "\"North, Court\",9,\"Cashier \"\"A\"\"\",02/05/2026,FA,*ACC123 - Auto cash input,NC,12.30,R,12.30";
    private static final String OLDER_CSV_ROW = "Westshire,17,Pedro,01/05/2026,SA,MISC123,PO,7.50,,99.00";

    private static final ReportInstanceEntity REPORT_INSTANCE = new ReportInstanceEntity();

    private final CashTillReportService service = new CashTillReportService();

    @Test
    void getReportId_returnsCashTill() {
        assertThat(service.getReportId()).isEqualTo(CASH_TILL);
    }

    @Test
    void getNumberOfRecords_returnsRowCount() {
        CashTillReportData reportData = reportDataWithRows(List.of(validRow(), validRow()));

        assertThat(reportData.getNumberOfRecords()).isEqualTo(2);
    }

    @Test
    void getNumberOfRecords_whenRowsAreNull_returnsZero() {
        CashTillReportData reportData = reportDataWithRows(null);

        assertThat(reportData.getNumberOfRecords()).isZero();
    }

    @Test
    void getReportMetaData_whenMetadataIsNull_returnsEmptyMetadata() {
        CashTillReportData reportData = CashTillReportData.builder().reportMetaData(null).build();

        assertThat(reportData.getReportMetaData().getPdpoPartyIds()).isEmpty();
    }

    @Test
    void convertReportDataToFileType_whenValidData_returnsF061Csv() {
        CashTillReportData reportData = allocatedReportData(List.of(olderRow(), newerRow()));

        String csv = convert(reportData);

        assertThat(csv).isEqualTo(String.join("\n", CSV_HEADER, NEWER_CSV_ROW, OLDER_CSV_ROW, ""));
    }

    @Test
    void convertReportDataToFileType_whenRowsAreEmpty_returnsHeaderOnly() {
        CashTillReportData reportData = reportDataWithRows(List.of());

        String csv = convert(reportData);

        assertThat(csv).isEqualTo(CSV_HEADER + "\n");
    }

    @Test
    void convertReportDataToFileType_doesNotMutateInputRows() {
        CashTillReportRow olderRow = olderRow();
        CashTillReportRow newerRow = newerRow();
        List<CashTillReportRow> rows = new ArrayList<>(List.of(olderRow, newerRow));
        CashTillReportData reportData = allocatedReportData(rows);

        convert(reportData);

        assertThat(reportData.getRows()).containsExactly(olderRow, newerRow);
    }

    @Test
    void convertReportDataToFileType_whenFileTypeIsUnsupported_throwsException() {
        CashTillReportData reportData = reportData(validRow());

        assertThatThrownBy(() -> service.convertReportDataToFileType(REPORT_INSTANCE, reportData, PDF))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Cash Till report only supports CSV conversion");
    }

    @Test
    void convertReportDataToFileType_whenReportDataIsNull_throwsException() {
        assertThatThrownBy(() -> service.convertReportDataToFileType(REPORT_INSTANCE, null, CSV))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Cash Till report data is required");
    }

    @Test
    void convertReportDataToFileType_whenRowsAreNull_throwsException() {
        CashTillReportData reportData = reportDataWithRows(null);

        assertThatThrownBy(() -> convert(reportData))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Cash Till report rows are required");
    }

    @Test
    void convertReportDataToFileType_whenRowIsNull_throwsException() {
        List<CashTillReportRow> rows = new ArrayList<>();
        rows.add(null);
        CashTillReportData reportData = reportDataWithRows(rows);

        assertThatThrownBy(() -> convert(reportData))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Cash Till report row 1 is required");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("missingFieldCases")
    void convertReportDataToFileType_whenMandatoryFieldIsMissing_throwsException(String description,
        Consumer<CashTillReportRow> change, String expectedMessage) {
        CashTillReportRow row = validRow();
        change.accept(row);

        assertThatThrownBy(() -> convert(reportData(row)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage(expectedMessage);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidMoneyCases")
    void convertReportDataToFileType_whenMoneyHasMoreThanTwoDecimalPlaces_throwsException(String description,
        Consumer<CashTillReportRow> change, String expectedMessage) {
        CashTillReportRow row = validRow();
        change.accept(row);

        assertThatThrownBy(() -> convert(reportData(row)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage(expectedMessage);
    }

    @Test
    void cashTillReportData_mapsSnakeCaseJson() throws Exception {
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

        CashTillReportData reportData = mapper.readValue("""
            {
              "allocated_report": true,
              "rows": [
                {
                  "business_unit": "Central",
                  "cash_till_number": "22",
                  "cashier": "Alex",
                  "payment_date_time": "2026-05-03T10:15:00",
                  "destination_type": "FA",
                  "details": "ACC999",
                  "auto_payment": false,
                  "payment_method": "CQ",
                  "amount": 1.20,
                  "receipt": true,
                  "balance": 1.20,
                  "allocated": false
                }
              ]
            }
            """, CashTillReportData.class);

        assertThat(reportData.getAllocatedReport()).isTrue();
        assertThat(reportData.getNumberOfRecords()).isEqualTo(1);
        assertThat(reportData.getRows().getFirst().getCashTillNumber()).isEqualTo("22");
        assertThat(reportData.getRows().getFirst().getDestinationType()).isEqualTo(CashTillDestinationType.FA);
        assertThat(reportData.getRows().getFirst().getPaymentMethod()).isEqualTo(CashTillPaymentMethod.CQ);
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

    private String convert(CashTillReportData reportData) {
        byte[] csv = service.convertReportDataToFileType(REPORT_INSTANCE, reportData, CSV);
        return new String(csv, StandardCharsets.UTF_8);
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
