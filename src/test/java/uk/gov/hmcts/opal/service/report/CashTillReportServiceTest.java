package uk.gov.hmcts.opal.service.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.opal.service.report.ReportId.CASH_TILL;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;
import uk.gov.hmcts.opal.dto.PdplIdentifierType;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.entity.DestinationType;
import uk.gov.hmcts.opal.entity.MiscellaneousAccountEntity;
import uk.gov.hmcts.opal.entity.PaymentInEntity;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.entity.TillEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.PaymentMethod;
import uk.gov.hmcts.opal.logging.integration.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.repository.MiscellaneousAccountRepository;
import uk.gov.hmcts.opal.repository.PaymentInRepository;
import uk.gov.hmcts.opal.repository.TillRepository;

class CashTillReportServiceTest {

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
        CashTillReportData reportData = reportDataWithRows(List.of(new CashTillReportRow(), new CashTillReportRow()));

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
    void generateReportData_whenPaymentMethodIsMissing_returnsRowWithNullPaymentMethod() {
        TillEntity till = till();
        PaymentInEntity payment = defendantPayment();
        payment.setPaymentMethod(null);
        ReportInstanceEntity reportInstance = reportInstance("""
            {"till_id":321}
            """);

        when(tillRepository.findById(321L)).thenReturn(Optional.of(till));
        when(paymentInRepository.findAll(anySpecification(), any(Sort.class))).thenReturn(List.of(payment));
        when(defendantAccountRepository.findAllByDefendantAccountIdIn(List.of(11L)))
            .thenReturn(List.of(defendantAccount()));

        CashTillReportData reportData = service.generateReportData(reportInstance);

        assertThat(reportData.getRows()).singleElement().satisfies(row ->
            assertThat(row.getPaymentMethod()).isNull());
    }

    @Test
    void generateReportData_whenDestinationTypeIsUnsupported_throwsException() {
        TillEntity till = till();
        PaymentInEntity payment = defendantPayment();
        payment.setDestinationType(DestinationType.C);
        ReportInstanceEntity reportInstance = reportInstance("""
            {"till_id":321}
            """);

        when(tillRepository.findById(321L)).thenReturn(Optional.of(till));
        when(paymentInRepository.findAll(anySpecification(), any(Sort.class))).thenReturn(List.of(payment));
        when(defendantAccountRepository.findAllByDefendantAccountIdIn(List.of(11L)))
            .thenReturn(List.of(defendantAccount()));

        assertThatThrownBy(() -> service.generateReportData(reportInstance))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Unsupported Cash Till destination type: C");
    }

    @Test
    void generateReportData_whenAssociatedRecordTypeIsMissing_throwsException() {
        TillEntity till = till();
        PaymentInEntity payment = defendantPayment();
        payment.setAssociatedRecordType(null);
        ReportInstanceEntity reportInstance = reportInstance("""
            {"till_id":321}
            """);

        when(tillRepository.findById(321L)).thenReturn(Optional.of(till));
        when(paymentInRepository.findAll(anySpecification(), any(Sort.class))).thenReturn(List.of(payment));

        assertThatThrownBy(() -> service.generateReportData(reportInstance))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Cash Till payment 1001 is missing associated_record_type");
    }

    @Test
    void generateReportData_whenAssociatedRecordTypeIsUnsupported_throwsException() {
        TillEntity till = till();
        PaymentInEntity payment = defendantPayment();
        payment.setAssociatedRecordType(AssociatedRecordType.SUSPENSE_ITEMS);
        ReportInstanceEntity reportInstance = reportInstance("""
            {"till_id":321}
            """);

        when(tillRepository.findById(321L)).thenReturn(Optional.of(till));
        when(paymentInRepository.findAll(anySpecification(), any(Sort.class))).thenReturn(List.of(payment));

        assertThatThrownBy(() -> service.generateReportData(reportInstance))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Cash Till payment 1001 has unsupported associated_record_type suspense_items");
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

    private static ReportInstanceEntity reportInstance(String reportParameters) {
        ReportInstanceEntity reportInstance = new ReportInstanceEntity();
        reportInstance.setReportParameters(reportParameters);
        return reportInstance;
    }

    private static CashTillReportData reportDataWithRows(List<CashTillReportRow> rows) {
        return CashTillReportData.builder()
            .rows(rows)
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
            .paymentMethod(PaymentMethod.NC)
            .destinationType(DestinationType.F)
            .associatedRecordType(AssociatedRecordType.DEFENDANT_ACCOUNTS)
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
            .paymentMethod(PaymentMethod.CQ)
            .destinationType(DestinationType.S)
            .associatedRecordType(AssociatedRecordType.MISCELLANEOUS_ACCOUNTS)
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
