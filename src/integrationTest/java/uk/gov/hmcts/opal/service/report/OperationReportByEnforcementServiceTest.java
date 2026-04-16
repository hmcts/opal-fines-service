package uk.gov.hmcts.opal.service.report;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;

@Slf4j(topic = "opal.OperationReportByEnforcementServiceTest")
@DisplayName("OperationReportByEnforcementServiceTest")
public class OperationReportByEnforcementServiceTest extends AbstractIntegrationTest {

    @Autowired
    private OperationReportByEnforcementService service;

    @MockitoBean
    private DefendantAccountRepository defendantAccountRepository;

    @MockitoBean
    private ReportResultMapper resultMapper;

    @Mock
    private ReportDataInterface mapped;

    @Test
    void generateReportData_shouldReadParameters_callRepositoryWithSort_andMapResults() {
        //Arrange
        ReportInstanceEntity reportInstance = mock(ReportInstanceEntity.class);
        given(reportInstance.getReportParameters()).willReturn("""
            {
              "report_type": "Detailed",
              "business_unit_ids": [10, 20],
              "enforcementMode": "all",
              "includeAdult": true,
              "includeYouth": false,
              "includeCompany": true,
              "collectionOrderChoice": "ALL",
              "accountStatus": "OPEN"
            }
            """);

        List<DefendantAccountEntity> accounts = List.of(
            mock(DefendantAccountEntity.class),
            mock(DefendantAccountEntity.class)
        );

        given(defendantAccountRepository.findAll(ArgumentMatchers.<Specification<DefendantAccountEntity>>any(),
            any(Sort.class))).willReturn(accounts);
        given(resultMapper.map(accounts)).willReturn((OperationReportByEnforcementTransaction) mapped);

        //Act
        ReportDataInterface result = service.generateReportData(reportInstance);

        //Assert
        assertThat(result).isSameAs(mapped);
        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        verify(defendantAccountRepository).findAll(
            ArgumentMatchers.<Specification<DefendantAccountEntity>>any(), sortCaptor.capture());
        assertThat(sortCaptor.getValue()).isEqualTo(Sort.by(Sort.Direction.ASC, "accountNumber"));
        verify(resultMapper).map(accounts);
    }

    @Test
    void generateReportData_shouldUseDefaultsWhenOptionalFieldsAreMissing() {
        //Arrange
        ReportInstanceEntity reportInstance = mock(ReportInstanceEntity.class);
        given(reportInstance.getReportParameters()).willReturn("""
            {
              "business_unit_ids": [99]
            }
            """);

        List<DefendantAccountEntity> accounts = List.of();

        given(defendantAccountRepository.findAll(
            ArgumentMatchers.<Specification<DefendantAccountEntity>>any(), any(Sort.class)))
            .willReturn(accounts);
        given(resultMapper.map(accounts)).willReturn((OperationReportByEnforcementTransaction) mapped);

        //Act
        ReportDataInterface result = service.generateReportData(reportInstance);

        //Assert
        assertThat(result).isSameAs(mapped);
        verify(defendantAccountRepository).findAll(
            ArgumentMatchers.<Specification<DefendantAccountEntity>>any(), any(Sort.class));
        verify(resultMapper).map(accounts);
    }
}


