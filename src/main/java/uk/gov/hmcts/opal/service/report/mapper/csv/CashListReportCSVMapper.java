package uk.gov.hmcts.opal.service.report.mapper.csv;

import static uk.gov.hmcts.opal.service.report.CommonReportHelper.formatMoney;
import static uk.gov.hmcts.opal.service.report.CommonReportHelper.getDataValue;
import static uk.gov.hmcts.opal.service.report.CommonReportStringConstants.NEW_LINE;

import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.service.report.CashListReportData;
import uk.gov.hmcts.opal.service.report.CashListReportData.CashListEntry;

@Component
public class CashListReportCSVMapper implements ReportCSVMapper<CashListReportData> {

    @Override
    public Class<CashListReportData> getReportDataType() {
        return CashListReportData.class;
    }

    @Override
    public String reportToCSVString(CashListReportData cashListReportData) {
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
