package uk.gov.hmcts.opal.service.report.mapper.csv;

import static uk.gov.hmcts.opal.service.report.CommonReportStringConstants.NEW_LINE;

import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.service.report.CashListReportData;
import uk.gov.hmcts.opal.service.report.CashListReportData.CashListEntry;
import uk.gov.hmcts.opal.service.report.CommonReportHelper;

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
        sb.append(cashListReportData.getTotal().toString());
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
            cashListEntry.getType(),
            cashListEntry.getSuspense(),
            cashListEntry.getAccountNumber(),
            cashListEntry.getName(),
            cashListEntry.getNameAdditionalInformation(),
            cashListEntry.getPaymentMethod(),
            cashListEntry.getAmount().toString());
    }
}
