package uk.gov.hmcts.opal.service.report.mapper.csv;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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

    @Test
    void reportToCSVString_whenReportDataIsNull_throwsMeaningfulException() {
        assertThatThrownBy(() -> mapper.reportToCSVString(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Cash List report data is required");
    }

    @Test
    void reportToCSVString_whenEntriesAreNull_throwsMeaningfulException() {
        CashListReportData reportData = CashListReportData.builder().total(money("0.00")).build();

        assertThatThrownBy(() -> mapper.reportToCSVString(reportData))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Cash List report entries are required");
    }

    @Test
    void reportToCSVString_whenEntryIsNull_throwsMeaningfulException() {
        List<CashListEntry> entries = new ArrayList<>();
        entries.add(null);
        CashListReportData reportData = CashListReportData.builder()
            .entries(entries)
            .total(money("0.00"))
            .build();

        assertThatThrownBy(() -> mapper.reportToCSVString(reportData))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Cash List report entry 1 is required");
    }

    @Test
    void reportToCSVString_whenTotalIsNull_throwsMeaningfulException() {
        CashListReportData reportData = CashListReportData.builder().entries(List.of()).build();

        assertThatThrownBy(() -> mapper.reportToCSVString(reportData))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Cash List report total is required");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidMoneyCases")
    void reportToCSVString_whenMoneyScaleExceedsTwoDecimalPlaces_throwsMeaningfulException(
        String description, CashListReportData reportData, String expectedMessage) {

        assertThatThrownBy(() -> mapper.reportToCSVString(reportData))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage(expectedMessage);
    }

    private static Stream<Arguments> invalidMoneyCases() {
        CashListEntry entryWithInvalidAmount = entry("FA", "", "ACC123", "Smith", "", "NC", "1.234");
        CashListReportData invalidEntryAmount = CashListReportData.builder()
            .entries(List.of(entryWithInvalidAmount))
            .total(money("1.23"))
            .build();
        CashListReportData invalidTotal = CashListReportData.builder()
            .entries(List.of())
            .total(money("1.234"))
            .build();

        return Stream.of(
            Arguments.of("entry amount", invalidEntryAmount,
                "Amount must not have more than two decimal places at Cash List report row 1"),
            Arguments.of("total", invalidTotal,
                "Cash List report total must not have more than two decimal places"));
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
