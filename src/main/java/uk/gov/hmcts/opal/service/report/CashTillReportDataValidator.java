package uk.gov.hmcts.opal.service.report;

import static uk.gov.hmcts.opal.service.report.CommonReportHelper.validateMoney;
import static uk.gov.hmcts.opal.service.report.CommonReportHelper.validateRequired;
import static uk.gov.hmcts.opal.service.report.CommonReportHelper.validateText;

public final class CashTillReportDataValidator {

    private static final String REPORT_NAME = "Cash Till report";

    private CashTillReportDataValidator() {
    }

    public static void validateReportData(CashTillReportData reportData) {
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
