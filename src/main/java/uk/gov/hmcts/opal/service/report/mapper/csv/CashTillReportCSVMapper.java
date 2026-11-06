package uk.gov.hmcts.opal.service.report.mapper.csv;

import static uk.gov.hmcts.opal.service.report.CashTillReportDataValidator.validateReportData;
import static uk.gov.hmcts.opal.service.report.CommonReportHelper.escapeCsv;
import static uk.gov.hmcts.opal.service.report.CommonReportHelper.formatMoney;
import static uk.gov.hmcts.opal.service.report.CommonReportStringConstants.COMMA;
import static uk.gov.hmcts.opal.service.report.CommonReportStringConstants.EMPTY_STRING;
import static uk.gov.hmcts.opal.service.report.CommonReportStringConstants.NEW_LINE;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.service.report.CashTillReportData;
import uk.gov.hmcts.opal.service.report.CashTillReportRow;

@Component
public class CashTillReportCSVMapper implements ReportCSVMapper<CashTillReportData> {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final List<String> HEADINGS = List.of(
        CashTillField.BUSINESS_UNIT.label(), CashTillField.CASH_TILL_NUMBER.label(),
        CashTillField.CASHIER.label(), CashTillField.DATE.label(), CashTillField.TYPE.label(),
        CashTillField.DETAILS.label(), CashTillField.PAYMENT_TYPE.label(), CashTillField.AMOUNT.label(),
        CashTillField.RECEIPT.label(), CashTillField.BALANCE.label());
    private static final String AUTO_CASH_INPUT_SUFFIX = " - Auto cash input";
    private static final String ALLOCATED_PAYMENT_PREFIX = "*";

    @Override
    public Class<CashTillReportData> getReportDataType() {
        return CashTillReportData.class;
    }

    @Override
    public String reportToCSVString(CashTillReportData reportData) {
        validateReportData(reportData);
        StringBuilder csv = new StringBuilder(String.join(COMMA, HEADINGS)).append(NEW_LINE);
        reportData.getRows().stream()
            .sorted(Comparator.comparing(CashTillReportRow::getPaymentDateTime).reversed())
            .map(row -> toCsvRow(reportData, row))
            .forEach(row -> csv.append(row).append(NEW_LINE));

        return csv.toString();
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
