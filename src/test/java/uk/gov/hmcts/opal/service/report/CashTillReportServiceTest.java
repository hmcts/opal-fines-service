package uk.gov.hmcts.opal.service.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.opal.service.report.FileType.CSV;
import static uk.gov.hmcts.opal.service.report.FileType.PDF;
import static uk.gov.hmcts.opal.service.report.ReportId.CASH_TILL;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;
import uk.gov.hmcts.opal.dto.PdplIdentifierType;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.entity.PaymentInEntity;
import uk.gov.hmcts.opal.entity.TillEntity;
import uk.gov.hmcts.opal.entity.MiscellaneousAccountEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.logging.integration.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.repository.MiscellaneousAccountRepository;
import uk.gov.hmcts.opal.repository.PaymentInRepository;
import uk.gov.hmcts.opal.repository.TillRepository;

class CashTillReportServiceTest {

    private static final String CSV_HEADER =
        "Business Unit,Cash Till Number,Cashier,Date,Type,Details,Payment Type,Amount,Receipt,Balance";
    private static final String NEWER_CSV_ROW =
        "\"North, Court\",9,\"Cashier \"\"A\"\"\",02/05/2026,FA,*ACC123 - Auto cash input,NC,12.30,R,12.30";
    private static final String OLDER_CSV_ROW = "Westshire,17,Pedro,01/05/2026,SA,MISC123,PO,7.50,,99.00";

    private static final ReportInstanceEntity REPORT_INSTANCE = new ReportInstanceEntity();
    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder()
        .findAndAddModules()
        .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
        .build();

    private final TillRepository tillRepository = mock(TillRepository.class);
    private final PaymentInRepository paymentInRepository = mock(PaymentInRepository.class);
    private final DefendantAccountRepository defendantAccountRepository = mock(DefendantAccountRepository.class);
    private final MiscellaneousAccountRepository miscellaneousAccountRepository =
        mock(MiscellaneousAccountRepository.class);
    private final CashTillReportDataMapper reportDataMapper = new CashTillReportDataMapper(
        defendantAccountRepository,
        miscellaneousAccountRepository);
    private final CashTillReportService service = new CashTillReportService(OBJECT_MAPPER,
        tillRepository,
        paymentInRepository,
        reportDataMapper);

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
    void generateReportData_returnsMappedRowsForAllPaymentsInTill() {
        TillEntity till = till();
        PaymentInEntity defendantPayment = defendantPayment();
        PaymentInEntity miscellaneousPayment = miscellaneousPayment();
        ReportInstanceEntity reportInstance = reportInstance("""
            {"till_id":321,"allocated_report":true}
            """);

        when(tillRepository.findById(321L)).thenReturn(Optional.of(till));
        when(paymentInRepository.findAll(anySpecification(), any(Sort.class)))
            .thenReturn(List.of(defendantPayment, miscellaneousPayment));
        when(defendantAccountRepository.findAllByDefendantAccountIdIn(List.of(11L)))
            .thenReturn(List.of(defendantAccount()));
        when(miscellaneousAccountRepository.findAllByMiscellaneousAccountIdIn(List.of(22L)))
            .thenReturn(List.of(miscellaneousAccount()));

        CashTillReportData reportData = service.generateReportData(reportInstance);

        assertThat(reportData.getAllocatedReport()).isTrue();
        assertThat(reportData.getRows()).hasSize(2);
        assertThat(reportData.getRows()).extracting(
            CashTillReportRow::getCashTillNumber,
            CashTillReportRow::getCashier,
            CashTillReportRow::getDestinationType,
            CashTillReportRow::getDetails,
            CashTillReportRow::getPaymentMethod,
            CashTillReportRow::getAmount,
            CashTillReportRow::getReceipt,
            CashTillReportRow::getAllocated
        ).containsExactly(
            Tuple.tuple("7", "Jamie", CashTillDestinationType.FA, "ACC-001",
                CashTillPaymentMethod.NC, money("12.30"), true, true),
            Tuple.tuple("7", "Jamie", CashTillDestinationType.SA, "MISC-002",
                CashTillPaymentMethod.CQ, money("8.40"), false, false));
        assertThat(reportData.getRows()).extracting(CashTillReportRow::getPaymentDateTime).containsExactly(
            LocalDateTime.of(2026, 5, 3, 10, 15),
            LocalDateTime.of(2026, 5, 2, 9, 5));
        assertThat(reportData.getReportMetaData().getPdpoPartyIds())
            .containsExactly(new ParticipantIdentifier("11", PdplIdentifierType.DEFENDANT_ACCOUNT));

        verify(tillRepository).findById(321L);
        verify(paymentInRepository).findAll(anySpecification(), any(Sort.class));
        verify(defendantAccountRepository).findAllByDefendantAccountIdIn(List.of(11L));
        verify(miscellaneousAccountRepository).findAllByMiscellaneousAccountIdIn(List.of(22L));
    }

    @Test
    void generateReportData_whenParametersCannotBeParsed_throwsException() {
        ReportInstanceEntity reportInstance = reportInstance("{");

        assertThatThrownBy(() -> service.generateReportData(reportInstance))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Failed to parse Cash Till report parameters");
    }

    @Test
    void generateReportData_whenTillIdIsMissing_throwsException() {
        ReportInstanceEntity reportInstance = reportInstance("""
            {"allocated_report":true}
            """);

        assertThatThrownBy(() -> service.generateReportData(reportInstance))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Cash Till report till_id is required");
    }

    @Test
    void generateReportData_whenTillCannotBeFound_throwsException() {
        ReportInstanceEntity reportInstance = reportInstance("""
            {"till_id":321}
            """);
        when(tillRepository.findById(321L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.generateReportData(reportInstance))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("Cash Till report till not found for till_id 321");
    }

    @Test
    void generateReportData_whenTillHasNoBusinessUnitName_throwsException() {
        TillEntity till = till();
        till.getBusinessUnit().setBusinessUnitName(" ");
        ReportInstanceEntity reportInstance = reportInstance("""
            {"till_id":321}
            """);

        when(tillRepository.findById(321L)).thenReturn(Optional.of(till));
        when(paymentInRepository.findAll(anySpecification(), any(Sort.class)))
            .thenReturn(List.of(defendantPayment()));
        when(defendantAccountRepository.findAllByDefendantAccountIdIn(List.of(11L)))
            .thenReturn(List.of(defendantAccount()));

        assertThatThrownBy(() -> service.generateReportData(reportInstance))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Cash Till report till is missing a business unit name");
    }

    @Test
    void generateReportData_whenAssociatedRecordIdIsInvalid_throwsException() {
        TillEntity till = till();
        PaymentInEntity payment = defendantPayment();
        payment.setAssociatedRecordId("abc");
        ReportInstanceEntity reportInstance = reportInstance("""
            {"till_id":321}
            """);

        when(tillRepository.findById(321L)).thenReturn(Optional.of(till));
        when(paymentInRepository.findAll(anySpecification(), any(Sort.class))).thenReturn(List.of(payment));

        assertThatThrownBy(() -> service.generateReportData(reportInstance))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Cash Till payment 1001 has invalid associated_record_id abc");
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
        ObjectMapper mapper = JsonMapper.builder()
            .findAndAddModules()
            .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();

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

    private static ReportInstanceEntity reportInstance(String reportParameters) {
        ReportInstanceEntity reportInstance = new ReportInstanceEntity();
        reportInstance.setReportParameters(reportParameters);
        return reportInstance;
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

    private static Specification<PaymentInEntity> anySpecification() {
        return any();
    }

    private static TillEntity till() {
        return TillEntity.builder()
            .tillId(321L)
            .tillNumber((short) 7)
            .ownedBy("Jamie")
            .businessUnit(BusinessUnitEntity.builder()
                .businessUnitId((short) 66)
                .businessUnitName("Central Unit")
                .build())
            .build();
    }

    private static PaymentInEntity defendantPayment() {
        return PaymentInEntity.builder()
            .paymentInId(1001L)
            .paymentDate(LocalDateTime.of(2026, 5, 3, 10, 15))
            .paymentAmount(money("12.30"))
            .paymentMethod("NC")
            .destinationType("F")
            .associatedRecordType(AssociatedRecordType.DEFENDANT_ACCOUNTS.getLabel())
            .associatedRecordId("11")
            .receipt(true)
            .autoPayment(false)
            .allocated(true)
            .build();
    }

    private static PaymentInEntity miscellaneousPayment() {
        return PaymentInEntity.builder()
            .paymentInId(1002L)
            .paymentDate(LocalDateTime.of(2026, 5, 2, 9, 5))
            .paymentAmount(money("8.40"))
            .paymentMethod("CQ")
            .destinationType("S")
            .associatedRecordType(AssociatedRecordType.MISCELLANEOUS_ACCOUNTS.getLabel())
            .associatedRecordId("22")
            .receipt(false)
            .autoPayment(true)
            .allocated(false)
            .build();
    }

    private static DefendantAccountEntity defendantAccount() {
        return DefendantAccountEntity.builder()
            .defendantAccountId(11L)
            .accountNumber("ACC-001")
            .build();
    }

    private static MiscellaneousAccountEntity miscellaneousAccount() {
        return MiscellaneousAccountEntity.builder()
            .miscellaneousAccountId(22L)
            .accountNumber("MISC-002")
            .build();
    }
}
