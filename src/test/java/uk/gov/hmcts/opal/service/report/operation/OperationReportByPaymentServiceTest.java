package uk.gov.hmcts.opal.service.report.operation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.opal.dto.ResultId.ABDC;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.dto.ResultId;
import uk.gov.hmcts.opal.dto.report.operation.OperationReportByPaymentFiltersDto;
import uk.gov.hmcts.opal.dto.report.operation.PaymentReportMode;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.repository.DefendantTransactionRepository;
import uk.gov.hmcts.opal.repository.EnforcementRepository;
import uk.gov.hmcts.opal.service.report.FileType;
import uk.gov.hmcts.opal.service.report.ReportDataInterface;
import uk.gov.hmcts.opal.service.report.ReportId;
import uk.gov.hmcts.opal.service.report.operation.mapper.DetailedResultMapper;

@ExtendWith(MockitoExtension.class)
class OperationReportByPaymentServiceTest {

    @Mock
    DefendantAccountRepository defendantAccountRepository;

    @Mock
    DefendantTransactionRepository defendantTransactionRepository;

    @Mock
    EnforcementRepository enforcementRepository;

    @Mock
    DetailedResultMapper detailedResultMapper;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    OperationReportByPaymentValidator validator;

    @Mock
    private OperationDetailedReport mappedDetailedReport;

    @Mock
    private DefendantAccountEntity account;

    @InjectMocks
    private OperationReportByPaymentService service;

    @Test
    void getReportId_returnsOpEnforcement() {
        assertThat(service.getReportId()).isEqualTo(ReportId.OP_PAYMENT);
    }

    @Test
    void generateReportData_failsValidation_throwsError() {
        ReportInstanceEntity reportInstance = mockReportInstance("""
            {}
            """);
        OperationReportByPaymentFiltersDto filters = OperationReportByPaymentFiltersDto.builder()
            .build();
        when(objectMapper.readValue(any(String.class), eq(OperationReportByPaymentFiltersDto.class)))
            .thenReturn(filters);

        doThrow(new IllegalArgumentException("not valid"))
            .when(validator)
            .validate(filters);

        assertThatThrownBy(() -> service.generateReportData(reportInstance))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("not valid");
    }

    @Test
    void generateReportData_whenReportParametersCannotBeRead_throwsRuntimeException() {
        ReportInstanceEntity reportInstance = mockReportInstance("{ invalid json }");

        when(objectMapper.readValue(any(String.class), eq(OperationReportByPaymentFiltersDto.class)))
            .thenThrow(new RuntimeException());

        assertThatThrownBy(() -> service.generateReportData(reportInstance))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Failed to parse report filters");

        verifyNoInteractions(defendantAccountRepository, defendantTransactionRepository, enforcementRepository,
            detailedResultMapper);
    }

    @Test
    void generateReportData_onlyAccountSpecs_onlyInteractsWithDefendantAccountRepository() {
        ReportInstanceEntity reportInstance = mockReportInstance("{ }");
        List<DefendantAccountEntity> accounts = List.of(mock(DefendantAccountEntity.class));
        OperationReportByPaymentFiltersDto filters =
            OperationReportByPaymentFiltersDto.builder().reportMode(PaymentReportMode.SINCE_DATE).build();
        when(objectMapper.readValue(any(String.class), eq(OperationReportByPaymentFiltersDto.class)))
            .thenReturn(filters);
        when(defendantAccountRepository.findAll(
            ArgumentMatchers.<Specification<DefendantAccountEntity>>any(),
            any(Sort.class)
        )).thenReturn(accounts);
        when(detailedResultMapper.map(accounts)).thenReturn(mappedDetailedReport);

        ReportDataInterface result = service.generateReportData(reportInstance);

        assertThat(result).isSameAs(mappedDetailedReport);
        Mockito.verify(defendantAccountRepository).findAll(
            ArgumentMatchers.<Specification<DefendantAccountEntity>>any(),
            any(Sort.class)
        );
        Mockito.verify(detailedResultMapper).map(accounts);
    }

    @Test
    void generateReportData_withRegf() {
        ReportInstanceEntity reportInstance = mockReportInstance("{ }");
        List<DefendantAccountEntity> accounts = List.of(account);
        OperationReportByPaymentFiltersDto filters = OperationReportByPaymentFiltersDto.builder()
            .reportMode(PaymentReportMode.WITH_REGF)
            .isPaymentMade(true)
            .build();
        when(objectMapper.readValue(any(String.class), eq(OperationReportByPaymentFiltersDto.class)))
            .thenReturn(filters);
        when(defendantAccountRepository.findAll(
            ArgumentMatchers.<Specification<DefendantAccountEntity>>any(),
            any(Sort.class)
        )).thenReturn(accounts);
        when(defendantAccountRepository.findAccountsWithPaymentMadeAfterFirstRegfEnforcement(true)).thenReturn(
            accounts);
        when(detailedResultMapper.map(any())).thenReturn(mappedDetailedReport);

        ReportDataInterface result = service.generateReportData(reportInstance);

        assertThat(result).isSameAs(mappedDetailedReport);
        Mockito.verify(defendantAccountRepository).findAll(
            ArgumentMatchers.<Specification<DefendantAccountEntity>>any(),
            any(Sort.class)
        );
        Mockito.verify(defendantAccountRepository).findAccountsWithPaymentMadeAfterFirstRegfEnforcement(true);
        Mockito.verify(detailedResultMapper).map(eq(accounts));
    }

    @Test
    void generateReportData_sinceLastEnforcement() {
        ReportInstanceEntity reportInstance = mockReportInstance("{ }");
        List<DefendantAccountEntity> accounts = List.of(account);
        OperationReportByPaymentFiltersDto filters = OperationReportByPaymentFiltersDto.builder()
            .sinceLastEnforcementAction(ABDC)
            .reportMode(PaymentReportMode.SINCE_LAST_ENFORCEMENT)
            .isPaymentMade(true)
            .build();
        when(objectMapper.readValue(any(String.class), eq(OperationReportByPaymentFiltersDto.class)))
            .thenReturn(filters);
        when(defendantAccountRepository.findAll(
            ArgumentMatchers.<Specification<DefendantAccountEntity>>any(),
            any(Sort.class)
        )).thenReturn(accounts);
        when(defendantAccountRepository.findAccountsWithPaymentMadeAfterLastEnforcementAction(ABDC.name(),
            true)).thenReturn(accounts);
        when(detailedResultMapper.map(any())).thenReturn(mappedDetailedReport);

        ReportDataInterface result = service.generateReportData(reportInstance);

        assertThat(result).isSameAs(mappedDetailedReport);
        Mockito.verify(defendantAccountRepository).findAll(
            ArgumentMatchers.<Specification<DefendantAccountEntity>>any(), any(Sort.class));
        Mockito.verify(defendantAccountRepository)
            .findAccountsWithPaymentMadeAfterLastEnforcementAction(ResultId.ABDC.name(), true);
        Mockito.verify(detailedResultMapper).map(eq(accounts));
    }

    @Test
    void convertReportDataToFileType() {
        assertThrows(UnsupportedOperationException.class,
            () -> service.convertReportDataToFileType(new ReportInstanceEntity(),
                new OperationDetailedReport(), FileType.CSV));
    }

    private ReportInstanceEntity mockReportInstance(String json) {
        ReportInstanceEntity reportInstance = mock(ReportInstanceEntity.class);
        when(reportInstance.getReportParameters()).thenReturn(json);
        return reportInstance;
    }
}