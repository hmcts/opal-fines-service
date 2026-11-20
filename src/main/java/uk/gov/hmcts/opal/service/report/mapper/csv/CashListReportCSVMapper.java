package uk.gov.hmcts.opal.service.report.mapper.csv;

import static uk.gov.hmcts.opal.service.report.CommonReportHelper.formatMoney;
import static uk.gov.hmcts.opal.service.report.CommonReportHelper.validateMoney;
import static uk.gov.hmcts.opal.service.report.CommonReportStringConstants.NEW_LINE;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.service.report.CashListReportData;
import uk.gov.hmcts.opal.service.report.CashListReportData.CashListEntry;

@Component
public class CashListReportCSVMapper implements ReportCSVMapper<CashListReportData> {

    private static final String REPORT_NAME = "Cash List report";

    @Override
    public Class<CashListReportData> getReportDataType() {
        return CashListReportData.class;
    }

    @Override
    public String reportToCSVString(ReportInstanceEntity instanceEntity, CashListReportData cashListReportData) {
        validateReportData(cashListReportData);
        StringBuilder sb = new StringBuilder();
        sb.append(dataListToFullCSVRow(headerRow()));
        int entryNumber = 1;
        for (CashListEntry cashListEntry : cashListReportData.getEntries()) {
            sb.append(dataListToFullCSVRow(convertDataRow(entryNumber++, cashListEntry)));
        }
        sb.append("Total").append(NEW_LINE);
        sb.append(getDataValue(formatMoney(cashListReportData.getTotal()))).append(NEW_LINE);
        return sb.toString();
    }

    private void validateReportData(CashListReportData reportData) {
        if (reportData == null) {
            throw new IllegalArgumentException(REPORT_NAME + " data is required");
        }
        if (reportData.getEntries() == null) {
            throw new IllegalArgumentException(REPORT_NAME + " entries are required");
        }
        validateTotal(reportData.getTotal());
        for (int entryIndex = 0; entryIndex < reportData.getEntries().size(); entryIndex++) {
            validateEntry(reportData.getEntries().get(entryIndex), entryIndex + 1);
        }
    }

    private void validateTotal(BigDecimal total) {
        if (total == null) {
            throw new IllegalArgumentException(REPORT_NAME + " total is required");
        }
        try {
            formatMoney(total);
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException(REPORT_NAME + " total must not have more than two decimal places", ex);
        }
    }

    private void validateEntry(CashListEntry entry, int entryNumber) {
        if (entry == null) {
            throw new IllegalArgumentException(REPORT_NAME + " entry " + entryNumber + " is required");
        }
        validateMoney(entry.getAmount(), "Amount", REPORT_NAME, entryNumber);
    }

    private List<String> headerRow() {
        return List.of("Entry",
            "Type",
            "Susp.",
            "Account no",
            "Name",
            "Name (Additional Information)",
            "Payment",
            "Amount");
    }

    private List<String> convertDataRow(int entryNumber, CashListEntry cashListEntry) {
        return List.of(String.valueOf(entryNumber),
            getDataValue(cashListEntry.getType()),
            getDataValue(cashListEntry.getSuspense()),
            getDataValue(cashListEntry.getAccountNumber()),
            getDataValue(cashListEntry.getName()),
            getDataValue(cashListEntry.getNameAdditionalInformation()),
            getDataValue(cashListEntry.getPaymentMethod()),
            getDataValue(formatMoney(cashListEntry.getAmount())));
    }
}
