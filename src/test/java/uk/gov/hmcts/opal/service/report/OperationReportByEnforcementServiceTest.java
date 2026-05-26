package uk.gov.hmcts.opal.service.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity;
import uk.gov.hmcts.opal.mapper.report.OperationReportByEnforcementResultMapper;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.repository.EnforcementRepository;

@ExtendWith(MockitoExtension.class)
class OperationReportByEnforcementServiceTest {

    @Mock
    private DefendantAccountRepository defendantAccountRepository;

    @Mock
    private EnforcementRepository enforcementRepository;

    @Mock
    private OperationReportByEnforcementResultMapper resultMapper;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private OperationReportByEnforcementValidator validator;

    @InjectMocks
    private OperationReportByEnforcementService service;

    @Test
    void getReportId_returnsOpEnforcement() {
        assertThat(service.getReportId()).isEqualTo(ReportId.OP_ENFORCEMENT);
    }

    @Test
    void generateReportData_whenNotFilteringOnEnforcementData_usesAccountRepositoryOnly() {
        ReportInstanceEntity reportInstance = mockReportInstance("""
            { "reportEnforcementMode": "NOT_UNDER_ENFORCEMENT" }
            """);

        OperationReportByEnforcementFiltersDto filters = OperationReportByEnforcementFiltersDto.builder()
            .reportEnforcementMode(ReportEnforcementMode.NOT_UNDER_ENFORCEMENT)
            .build();

        List<DefendantAccountEntity> accounts = List.of(mock(DefendantAccountEntity.class));
        OperationReportByEnforcementTransaction mapped = mock(OperationReportByEnforcementTransaction.class);

        when(objectMapper.readValue(any(String.class), eq(OperationReportByEnforcementFiltersDto.class)))
            .thenReturn(filters);
        when(defendantAccountRepository.findAll(
            ArgumentMatchers.<Specification<DefendantAccountEntity>>any(),
            any(Sort.class)
        )).thenReturn(accounts);
        when(resultMapper.map(accounts)).thenReturn(mapped);

        ReportDataInterface result = service.generateReportData(reportInstance);

        assertThat(result).isSameAs(mapped);
        verify(defendantAccountRepository).findAll(
            ArgumentMatchers.<Specification<DefendantAccountEntity>>any(),
            any(Sort.class)
        );
        verifyNoInteractions(enforcementRepository);
        verify(resultMapper).map(accounts);
    }

    @Test
    void generateReportData_whenAllModeWithNoEnforcementDates_usesAccountRepositoryOnly() {
        ReportInstanceEntity reportInstance = mockReportInstance("""
            { "reportEnforcementMode": "ALL" }
            """);

        OperationReportByEnforcementFiltersDto filters = OperationReportByEnforcementFiltersDto.builder()
            .reportEnforcementMode(ReportEnforcementMode.ALL)
            .build();

        List<DefendantAccountEntity> accounts = List.of(mock(DefendantAccountEntity.class));
        OperationReportByEnforcementTransaction mapped = mock(OperationReportByEnforcementTransaction.class);

        when(objectMapper.readValue(any(String.class), eq(OperationReportByEnforcementFiltersDto.class))).thenReturn(
            filters);
        when(defendantAccountRepository.findAll(
            ArgumentMatchers.<Specification<DefendantAccountEntity>>any(),
            any(Sort.class)
        )).thenReturn(accounts);
        when(resultMapper.map(accounts)).thenReturn(mapped);

        ReportDataInterface result = service.generateReportData(reportInstance);

        assertThat(result).isSameAs(mapped);
        verify(defendantAccountRepository).findAll(
            ArgumentMatchers.<Specification<DefendantAccountEntity>>any(),
            any(Sort.class)
        );
        verifyNoInteractions(enforcementRepository);
        verify(resultMapper).map(accounts);
    }

    @Test
    void generateReportData_whenFilteringOnEnforcementData_usesEnforcementAndAccountRepositories() {
        ReportInstanceEntity reportInstance = mockReportInstance("""
            {
              "reportEnforcementMode": "LAST_ACTION",
              "enforcementAction": "REGF"
            }
            """);

        OperationReportByEnforcementFiltersDto filters = OperationReportByEnforcementFiltersDto.builder()
            .reportEnforcementMode(ReportEnforcementMode.LAST_ACTION)
            .enforcementAction("REGF")
            .build();

        DefendantAccountEntity account1 = mock(DefendantAccountEntity.class);
        DefendantAccountEntity account2 = mock(DefendantAccountEntity.class);

        when(account1.getDefendantAccountId()).thenReturn(77L);
        when(account2.getDefendantAccountId()).thenReturn(88L);

        EnforcementEntity enforcement1 = mock(EnforcementEntity.class);
        EnforcementEntity enforcement2 = mock(EnforcementEntity.class);

        when(enforcement1.getDefendantAccount()).thenReturn(account1);
        when(enforcement2.getDefendantAccount()).thenReturn(account2);

        List<EnforcementEntity> enforcements = List.of(enforcement1, enforcement2);
        List<DefendantAccountEntity> accounts = List.of(account1, account2);
        OperationReportByEnforcementTransaction mapped = mock(OperationReportByEnforcementTransaction.class);

        when(objectMapper.readValue(any(String.class), eq(OperationReportByEnforcementFiltersDto.class))).thenReturn(
            filters);
        when(enforcementRepository.findAll(
            ArgumentMatchers.<Specification<EnforcementEntity>>any()
        )).thenReturn(enforcements);
        when(defendantAccountRepository.findAll(
            ArgumentMatchers.<Specification<DefendantAccountEntity>>any(),
            any(Sort.class)
        )).thenReturn(accounts);
        when(resultMapper.map(accounts)).thenReturn(mapped);

        ReportDataInterface result = service.generateReportData(reportInstance);

        assertThat(result).isSameAs(mapped);
        verify(enforcementRepository).findAll(
            ArgumentMatchers.<Specification<EnforcementEntity>>any()
        );
        verify(defendantAccountRepository).findAll(
            ArgumentMatchers.<Specification<DefendantAccountEntity>>any(),
            any(Sort.class)
        );
        verify(resultMapper).map(accounts);
    }

    @Test
    void generateReportData_validationFails_ThrowsIllegalArgumentException() {
        ReportInstanceEntity reportInstance = mockReportInstance("""
            {
              "reportEnforcementMode": "LAST_ACTION"
            }
            """);
        OperationReportByEnforcementFiltersDto filters = OperationReportByEnforcementFiltersDto.builder()
            .reportEnforcementMode(ReportEnforcementMode.LAST_ACTION)
            .build();
        when(objectMapper.readValue(any(String.class), eq(OperationReportByEnforcementFiltersDto.class)))
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

        when(objectMapper.readValue(any(String.class), eq(OperationReportByEnforcementFiltersDto.class)))
            .thenThrow(new RuntimeException());

        assertThatThrownBy(() -> service.generateReportData(reportInstance))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Failed to parse report filters");

        verifyNoInteractions(defendantAccountRepository, enforcementRepository, resultMapper);
    }

    private ReportInstanceEntity mockReportInstance(String json) {
        ReportInstanceEntity reportInstance = mock(ReportInstanceEntity.class);
        when(reportInstance.getReportParameters()).thenReturn(json);
        return reportInstance;
    }
}