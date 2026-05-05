package uk.gov.hmcts.opal.service.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.opal.service.report.ReportId.OP_ENFORCEMENT;

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

    @InjectMocks
    private OperationReportByEnforcementService service;

    // ----------------------------------------
    // Helpers
    // ----------------------------------------
    private ReportInstanceEntity reportWithJson(String json) {
        ReportInstanceEntity instance = mock(ReportInstanceEntity.class);
        when(instance.getReportParameters()).thenReturn(json);
        return instance;
    }

    private DefendantAccountEntity account(String accNo) {
        DefendantAccountEntity acc = new DefendantAccountEntity();
        acc.setAccountNumber(accNo);
        return acc;
    }

    private EnforcementEntity enforcement(DefendantAccountEntity acc) {
        EnforcementEntity e = new EnforcementEntity();
        e.setDefendantAccount(acc);
        return e;
    }

    // ----------------------------------------
    // Tests
    // ----------------------------------------

    @Test
    void getReportId_returnsCorrectId() {
        assertThat(service.getReportId()).isEqualTo(OP_ENFORCEMENT);
    }

    @Test
    void generateReportData_notUnderEnforcement_usesAccountRepository() {
        // Arrange
        ReportFiltersDto filters = ReportFiltersDto.builder()
            .reportEnforcementMode(ReportEnforcementMode.NOT_UNDER_ENFORCEMENT)
            .build();
        when(objectMapper.readValue(anyString(), eq(ReportFiltersDto.class)))
            .thenReturn(filters);
        List<DefendantAccountEntity> accounts = List.of(
            account("A1"),
            account("A2")
        );
        when(defendantAccountRepository.findAll(
            ArgumentMatchers.<Specification<DefendantAccountEntity>>any(),
            any(Sort.class)
        )).thenReturn(accounts);
        OperationReportByEnforcementTransaction mapped =
            new OperationReportByEnforcementTransaction();
        when(resultMapper.map(accounts)).thenReturn(mapped);
        String json = """
                { "reportEnforcementMode": "NOT_UNDER_ENFORCEMENT" }
            """;

        // Act
        ReportDataInterface result =
            service.generateReportData(reportWithJson(json));

        // Assert
        assertThat(result).isSameAs(mapped);
        verify(defendantAccountRepository).findAll(
            ArgumentMatchers.<Specification<DefendantAccountEntity>>any(),
            any(Sort.class)
        );
        verify(enforcementRepository, never()).findAll(
            ArgumentMatchers.<Specification<EnforcementEntity>>any()
        );
        verify(resultMapper).map(accounts);
    }

    @Test
    void generateReportData_enforcementModes_useEnforcementRepository() {
        // Arrange
        ReportFiltersDto filters = ReportFiltersDto.builder()
            .reportEnforcementMode(ReportEnforcementMode.ALL)
            .build();
        when(objectMapper.readValue(anyString(), eq(ReportFiltersDto.class)))
            .thenReturn(filters);
        DefendantAccountEntity acc1 = account("B2");
        DefendantAccountEntity acc2 = account("A1");
        List<EnforcementEntity> enforcements = List.of(
            enforcement(acc1),
            enforcement(acc2),
            enforcement(acc1)
        );
        when(enforcementRepository.findAll(
            ArgumentMatchers.<Specification<EnforcementEntity>>any()
        )).thenReturn(enforcements);
        OperationReportByEnforcementTransaction mapped =
            new OperationReportByEnforcementTransaction();
        when(resultMapper.map(anyList())).thenReturn(mapped);
        String json = """
                { "reportEnforcementMode": "ALL" }
            """;
        // Act
        ReportDataInterface result =
            service.generateReportData(reportWithJson(json));
        // Assert
        assertThat(result).isSameAs(mapped);
        verify(defendantAccountRepository, never()).findAll(
            ArgumentMatchers.<Specification<DefendantAccountEntity>>any(),
            any(Sort.class)
        );
        verify(resultMapper).map(argThat(accounts ->
            accounts.stream()
                .map(DefendantAccountEntity::getAccountNumber)
                .toList()
                .equals(List.of("A1", "B2"))
        ));
    }

    @Test
    void generateReportData_sortsAccountsByAccountNumber() {
        // Arrange
        ReportFiltersDto filters = ReportFiltersDto.builder()
            .reportEnforcementMode(ReportEnforcementMode.ALL)
            .build();
        when(objectMapper.readValue(anyString(), eq(ReportFiltersDto.class)))
            .thenReturn(filters);
        DefendantAccountEntity acc1 = account("Z9");
        DefendantAccountEntity acc2 = account("A1");
        when(enforcementRepository.findAll(
            ArgumentMatchers.<Specification<EnforcementEntity>>any()
        )).thenReturn(List.of(enforcement(acc1), enforcement(acc2)));
        when(resultMapper.map(
            ArgumentMatchers.any()
        )).thenReturn(new OperationReportByEnforcementTransaction());
        String json = """
                { "reportEnforcementMode": "ALL" }
            """;
        // Act
        service.generateReportData(reportWithJson(json));
        verify(resultMapper).map(argThat(list ->
            list.stream()
                .map(DefendantAccountEntity::getAccountNumber)
                .toList()
                .equals(List.of("A1", "Z9"))
        ));
    }

    @Test
    void generateReportData_handlesInvalidJson_throwsException() {
        // Arrange
        ReportInstanceEntity instance = mock(ReportInstanceEntity.class);
        when(instance.getReportParameters()).thenReturn("invalid-json");
        when(objectMapper.readValue(anyString(), eq(ReportFiltersDto.class)))
            .thenThrow(new RuntimeException("boom"));
        // Act + Assert
        assertThatThrownBy(() -> service.generateReportData(instance))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Failed to parse report filters");
    }
}