package uk.gov.hmcts.opal.service.report;

import static uk.gov.hmcts.opal.service.report.CommonReportStringConstants.COMMA;
import static uk.gov.hmcts.opal.service.report.CommonReportStringConstants.DOUBLE_QUOTE;
import static uk.gov.hmcts.opal.service.report.CommonReportStringConstants.NEW_LINE;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class CommonReportHelper {

    private CommonReportHelper() {
        // utility class
    }

    public static String formatMoney(BigDecimal value) {
        return value.setScale(2, RoundingMode.UNNECESSARY).toPlainString();
    }

    public static String escapeCsv(String value) {
        if (value.contains(COMMA) || value.contains(DOUBLE_QUOTE) || value.contains(NEW_LINE)
            || value.contains("\r")) {
            return DOUBLE_QUOTE + value.replace(DOUBLE_QUOTE, DOUBLE_QUOTE + DOUBLE_QUOTE) + DOUBLE_QUOTE;
        }
        return value;
    }

    public static void validateRequired(Object value, String fieldName, String reportName, int rowNumber) {
        if (value == null) {
            throw requiredField(fieldName, reportName, rowNumber);
        }
    }

    public static void validateText(String value, String fieldName, String reportName, int rowNumber) {
        if (value == null || value.isBlank()) {
            throw requiredField(fieldName, reportName, rowNumber);
        }
    }

    public static void validateMoney(BigDecimal value, String fieldName, String reportName, int rowNumber) {
        if (value == null) {
            throw requiredField(fieldName, reportName, rowNumber);
        }
        try {
            formatMoney(value);
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException(
                fieldName + " must not have more than two decimal places at " + reportName + " row " + rowNumber, ex);
        }
    }

    private static IllegalArgumentException requiredField(String fieldName, String reportName, int rowNumber) {
        return new IllegalArgumentException(fieldName + " is required at " + reportName + " row " + rowNumber);
    }
}
