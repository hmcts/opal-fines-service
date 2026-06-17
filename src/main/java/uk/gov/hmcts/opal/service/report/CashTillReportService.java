package uk.gov.hmcts.opal.service.report;

import static uk.gov.hmcts.opal.service.report.CommonReportHelper.escapeCsv;
import static uk.gov.hmcts.opal.service.report.CommonReportHelper.formatMoney;
import static uk.gov.hmcts.opal.service.report.CommonReportHelper.validateMoney;
import static uk.gov.hmcts.opal.service.report.CommonReportHelper.validateRequired;
import static uk.gov.hmcts.opal.service.report.CommonReportHelper.validateText;
import static uk.gov.hmcts.opal.service.report.CommonReportStringConstants.COMMA;
import static uk.gov.hmcts.opal.service.report.CommonReportStringConstants.EMPTY_STRING;
import static uk.gov.hmcts.opal.service.report.CommonReportStringConstants.NEW_LINE;
import static uk.gov.hmcts.opal.service.report.FileType.CSV;
import static uk.gov.hmcts.opal.service.report.ReportId.CASH_TILL;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;

@Service
public class CashTillReportService implements ReportInterface<CashTillReportData> {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String REPORT_NAME = "Cash Till report";
    private static final List<String> HEADINGS = List.of(
        CashTillField.BUSINESS_UNIT.label(), CashTillField.CASH_TILL_NUMBER.label(),
        CashTillField.CASHIER.label(), CashTillField.DATE.label(), CashTillField.TYPE.label(),
        CashTillField.DETAILS.label(), CashTillField.PAYMENT_TYPE.label(), CashTillField.AMOUNT.label(),
        CashTillField.RECEIPT.label(), CashTillField.BALANCE.label());
    private static final String AUTO_CASH_INPUT_SUFFIX = " - Auto cash input";
    private static final String ALLOCATED_PAYMENT_PREFIX = "*";

    @Override
    public ReportId getReportId() {
        return CASH_TILL;
    }

    @Override
    public CashTillReportData generateReportData(ReportInstanceEntity reportInstance) {
        throw new UnsupportedOperationException("Cash Till report data generation is out of scope for PO-2637");
    }

    @Override
    public byte[] convertReportDataToFileType(ReportInstanceEntity reportInstance, CashTillReportData reportData,
        FileType fileType) {
        if (fileType != CSV) {
            throw new IllegalArgumentException("Cash Till report only supports CSV conversion");
        }

        validateReportData(reportData);
        StringBuilder csv = new StringBuilder(String.join(COMMA, HEADINGS)).append(NEW_LINE);
        reportData.getRows().stream()
            .sorted(Comparator.comparing(CashTillReportRow::getPaymentDateTime).reversed())
            .map(row -> toCsvRow(reportData, row))
            .forEach(row -> csv.append(row).append(NEW_LINE));

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static String toCsvRow(CashTillReportData reportData, CashTillReportRow row) {
        return String.join(COMMA, List.of(
            escapeCsv(row.getBusinessUnit()),
            escapeCsv(row.getCashTillNumber()),
            escapeCsv(row.getCashier()),
            escapeCsv(DATE_FORMATTER.format(row.getPaymentDateTime())),
            escapeCsv(row.getDestinationType().toValue()),
            escapeCsv(formatDetails(reportData, row)),
            escapeCsv(row.getPaymentMethod().toValue()),
            escapeCsv(formatMoney(row.getAmount())),
            escapeCsv(Boolean.TRUE.equals(row.getReceipt()) ? "R" : EMPTY_STRING),
            escapeCsv(formatMoney(row.getBalance()))));
    }

    private static String formatDetails(CashTillReportData reportData, CashTillReportRow row) {
        String details = row.getDetails();
        if (Boolean.TRUE.equals(reportData.getAllocatedReport()) && Boolean.TRUE.equals(row.getAllocated())) {
            details = ALLOCATED_PAYMENT_PREFIX + details;
        }
        if (Boolean.TRUE.equals(row.getAutoPayment())) {
            details += AUTO_CASH_INPUT_SUFFIX;
        }
        return details;
    }

    private static void validateReportData(CashTillReportData reportData) {
        if (reportData == null) {
            throw new IllegalArgumentException("Cash Till report data is required");
        }
        if (reportData.getRows() == null) {
            throw new IllegalArgumentException("Cash Till report rows are required");
        }
        for (int rowIndex = 0; rowIndex < reportData.getRows().size(); rowIndex++) {
            validateRow(reportData.getRows().get(rowIndex), rowIndex + 1);
        }
    }

    private static void validateRow(CashTillReportRow row, int rowNumber) {
        if (row == null) {
            throw new IllegalArgumentException("Cash Till report row " + rowNumber + " is required");
        }

        validateText(row.getBusinessUnit(), CashTillField.BUSINESS_UNIT.label(), REPORT_NAME, rowNumber);
        validateText(row.getCashTillNumber(), CashTillField.CASH_TILL_NUMBER.label(), REPORT_NAME, rowNumber);
        validateText(row.getCashier(), CashTillField.CASHIER.label(), REPORT_NAME, rowNumber);
        validateText(row.getDetails(), CashTillField.DETAILS.label(), REPORT_NAME, rowNumber);
        validateRequired(row.getDestinationType(), CashTillField.TYPE.label(), REPORT_NAME, rowNumber);
        validateRequired(row.getPaymentMethod(), CashTillField.PAYMENT_TYPE.label(), REPORT_NAME, rowNumber);
        validateDate(row, rowNumber);
        validateRequired(row.getAutoPayment(), "auto_payment", REPORT_NAME, rowNumber);
        validateRequired(row.getReceipt(), CashTillField.RECEIPT.label(), REPORT_NAME, rowNumber);
        validateMoney(row.getAmount(), CashTillField.AMOUNT.label(), REPORT_NAME, rowNumber);
        validateMoney(row.getBalance(), CashTillField.BALANCE.label(), REPORT_NAME, rowNumber);
    }

    private static void validateDate(CashTillReportRow row, int rowNumber) {
        if (row.getPaymentDateTime() == null) {
            throw new IllegalArgumentException(
                CashTillField.DATE.label() + " is required at Cash Till report row " + rowNumber);
        }
    }

    private enum CashTillField {
        BUSINESS_UNIT("Business Unit"),
        CASH_TILL_NUMBER("Cash Till Number"),
        CASHIER("Cashier"),
        DATE("Date"),
        TYPE("Type"),
        DETAILS("Details"),
        PAYMENT_TYPE("Payment Type"),
        AMOUNT("Amount"),
        RECEIPT("Receipt"),
        BALANCE("Balance");

        private final String label;

        CashTillField(String label) {
            this.label = label;
        }

        private String label() {
            return label;
        }
    }
}
