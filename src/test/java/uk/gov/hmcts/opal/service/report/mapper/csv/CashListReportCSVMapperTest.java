package uk.gov.hmcts.opal.service.report.mapper.csv;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.service.report.CashListReportData;
import uk.gov.hmcts.opal.service.report.CashListReportData.CashListEntry;

class CashListReportCSVMapperTest {

    private static final String HEADER =
        "Entry,Type,Susp.,Account no,Name,Name (Additional Information),Payment,Amount";

    private final CashListReportCSVMapper mapper = new CashListReportCSVMapper();

    @Test
    void getReportDataType_returnsCashListReportDataClass() {
        assertThat(mapper.getReportDataType()).isEqualTo(CashListReportData.class);
    }

    @Test
    void reportToCSVString_whenEntriesExist_returnsRowsAndTotal() {
        CashListReportData reportData = CashListReportData.builder()
            .entries(List.of(
                entry("Account", "", "ACC123", "Smith, Alex", "Guardian \"A\"", "CA", "12.30"),
                entry(null, null, null, null, null, "PO", "8.00")))
            .total(money("20.30"))
            .build();

        String csv = mapper.reportToCSVString(reportData);

        assertThat(csv).isEqualTo(String.join("\n",
            HEADER,
            "1,Account,,ACC123,\"Smith, Alex\",\"Guardian \"\"A\"\"\",CA,12.30",
            "2,,,,,,PO,8.00",
            "Total",
            "20.30",
            ""));
    }

    @Test
    void reportToCSVString_whenEntriesAreEmpty_returnsHeaderAndTotalOnly() {
        CashListReportData reportData = CashListReportData.builder()
            .entries(List.of())
            .total(money("0.00"))
            .build();

        String csv = mapper.reportToCSVString(reportData);

        assertThat(csv).isEqualTo(String.join("\n", HEADER, "Total", "0.00", ""));
    }

    private static CashListEntry entry(String type, String suspense, String accountNumber, String name,
        String nameAdditionalInformation, String paymentMethod, String amount) {
        return CashListEntry.builder()
            .type(type)
            .suspense(suspense)
            .accountNumber(accountNumber)
            .name(name)
            .nameAdditionalInformation(nameAdditionalInformation)
            .paymentMethod(paymentMethod)
            .amount(money(amount))
            .build();
    }

    private static BigDecimal money(String value) {
        return new BigDecimal(value);
    }
}
