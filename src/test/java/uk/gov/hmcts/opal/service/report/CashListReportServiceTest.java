package uk.gov.hmcts.opal.service.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.entity.PaymentInEntity;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.entity.TillEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.repository.PaymentInRepository;
import uk.gov.hmcts.opal.repository.TillRepository;

@ExtendWith(MockitoExtension.class)
class CashListReportServiceTest {

    private static final Long TILL_ID = 99000000010000L;

    @Mock
    private TillRepository tillRepository;

    @Mock
    private PaymentInRepository paymentInRepository;

    @Mock
    private CashListReportAssembler cashListReportAssembler;

    private CashListReportService service;
    private BusinessUnitEntity businessUnit;
    private TillEntity till;

    @BeforeEach
    void setUp() {
        service = new CashListReportService(
            new ObjectMapper(), tillRepository, paymentInRepository, cashListReportAssembler);
        businessUnit = BusinessUnitEntity.builder()
            .businessUnitId((short) 77)
            .businessUnitName("London Collection Unit")
            .businessUnitCode("LOND")
            .build();
        till = TillEntity.builder()
            .tillId(TILL_ID)
            .tillNumber((short) 9001)
            .ownedBy("L080JG")
            .businessUnit(businessUnit)
            .build();
    }

    @Test
    void generateReportData_loadsTillPaymentsAndDelegatesMapping() {
        // Arrange
        List<PaymentInEntity> payments = List.of(payment(1L), payment(2L));
        CashListReportData mappedData = CashListReportData.builder()
            .tillDetails(CashListReportData.TillDetails.builder().tillId(TILL_ID).build())
            .entries(List.of(new CashListReportData.CashListEntry(), new CashListReportData.CashListEntry()))
            .build();

        when(tillRepository.findById(TILL_ID)).thenReturn(Optional.of(till));
        when(paymentInRepository.findByTillEntity_TillIdOrderByPaymentDateAscPaymentInIdAsc(TILL_ID))
            .thenReturn(payments);
        when(cashListReportAssembler.toReportData(till, businessUnit, payments)).thenReturn(mappedData);

        // Act
        CashListReportData result = service.generateReportData(reportInstance("{\"till_id\":" + TILL_ID + "}"));

        // Assert
        assertThat(result).isSameAs(mappedData);
        verify(paymentInRepository).findByTillEntity_TillIdOrderByPaymentDateAscPaymentInIdAsc(TILL_ID);
        verify(cashListReportAssembler).toReportData(till, businessUnit, payments);
    }

    @ParameterizedTest
    @MethodSource("invalidTillIdParameters")
    void generateReportData_rejectsInvalidTillId(String reportParameters, String expectedMessage) {
        assertThatThrownBy(() -> service.generateReportData(reportInstance(reportParameters)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage(expectedMessage);
        verify(tillRepository, never()).findById(TILL_ID);
    }

    @Test
    void generateReportData_throwsWhenTillDoesNotExist() {
        when(tillRepository.findById(TILL_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.generateReportData(reportInstance("{\"till_id\":\"" + TILL_ID + "\"}")))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("Cash List report till not found for till_id: " + TILL_ID);
        verify(cashListReportAssembler, never()).toReportData(org.mockito.Mockito.any(), org.mockito.Mockito.any(),
            org.mockito.Mockito.any());
    }

    @Test
    void generateReportData_throwsWhenTillHasNoBusinessUnit() {
        till.setBusinessUnit(null);
        when(tillRepository.findById(TILL_ID)).thenReturn(Optional.of(till));

        assertThatThrownBy(() -> service.generateReportData(reportInstance("{\"till_id\":" + TILL_ID + "}")))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("Cash List report business unit not found for till_id: " + TILL_ID);
        verify(cashListReportAssembler, never()).toReportData(org.mockito.Mockito.any(), org.mockito.Mockito.any(),
            org.mockito.Mockito.any());
    }

    @Test
    void convertReportDataToFileType_returnsTemporaryStub() {
        byte[] result = service.convertReportDataToFileType(new ReportInstanceEntity(), new CashListReportData(),
            FileType.CSV);

        assertThat(result).isEmpty();
    }

    private static ReportInstanceEntity reportInstance(String reportParameters) {
        ReportInstanceEntity reportInstance = new ReportInstanceEntity();
        reportInstance.setReportParameters(reportParameters);
        return reportInstance;
    }

    private static Stream<Arguments> invalidTillIdParameters() {
        return Stream.of(
            Arguments.of("", "Cash List report requires a till_id report parameter"),
            Arguments.of(" ", "Cash List report requires a till_id report parameter"),
            Arguments.of("{}", "Cash List report requires a till_id report parameter"),
            Arguments.of("{\"till_id\":null}", "Cash List report requires a till_id report parameter"),
            Arguments.of("{\"till_id\":\"abc\"}", "Cash List report parameter till_id must be a whole number"),
            Arguments.of("{\"till_id\":\"\"}", "Cash List report parameter till_id must be a whole number"),
            Arguments.of("{\"till_id\":1.5}", "Cash List report parameter till_id must be a whole number"),
            Arguments.of("{\"till_id\":0}", "Cash List report parameter till_id must be greater than zero"),
            Arguments.of("{\"till_id\":-1}", "Cash List report parameter till_id must be greater than zero"),
            Arguments.of("{invalid", "Cash List report parameters must be valid JSON")
        );
    }

    private PaymentInEntity payment(Long paymentInId) {
        return PaymentInEntity.builder()
            .paymentInId(paymentInId)
            .tillEntity(till)
            .paymentDate(LocalDateTime.of(2026, 5, 26, 14, 30))
            .paymentMethod("CA")
            .destinationType("F")
            .build();
    }
}
