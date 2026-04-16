package uk.gov.hmcts.opal.service.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.opal.service.report.FileType.CSV;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;

@ExtendWith(MockitoExtension.class)
class OperationReportByEnforcementServiceTest {

    @Mock
    private DefendantAccountRepository defendantAccountRepository;

    @Mock
    private ReportResultMapper resultMapper;

    @InjectMocks
    private OperationReportByEnforcementService service;

    @Test
    void convertReportDataToFileType_returnsEmptyByteArray() {
        byte[] out = service.convertReportDataToFileType(null, null, CSV);
        assertThat(out).isNotNull();
        assertThat(out.length).isZero();
    }

    @Test
    void generateReportData_withEmptyJson_callsRepository_and_mapper_and_returns_mapper_result() {
        ReportInstanceEntity reportInstance = new ReportInstanceEntity();
        reportInstance.setReportParameters("{}");

        when(defendantAccountRepository.findAll(
            ArgumentMatchers.<Specification<DefendantAccountEntity>>any(),
            ArgumentMatchers.<Sort>any()
        )).thenReturn(Collections.emptyList());

        OperationReportByEnforcementTransaction expectedReport = new OperationReportByEnforcementTransaction();
        expectedReport.setTransactionList(Collections.emptyList());
        when(resultMapper.map(Collections.emptyList())).thenReturn(expectedReport);

        ReportDataInterface actual = service.generateReportData(reportInstance);

        assertThat(actual).isSameAs(expectedReport);
    }

    @Test
    void generateReportData_withFullParameters_parsesFilters_and_uses_repository_and_mapper() {
        String json = """
            {
              "Report_types": "Detailed",
              "business_unit_ids": [1, 2, 3],
              "enforcementMode": "LAST_ACTION",
              "enforcementDateFrom": "2024-01-01",
              "enforcementDateTo": "2024-12-31",
              "lastActionDateFrom": "2024-02-01",
              "lastActionDateTo": "2024-02-28",
              "regfDateFrom": "2024-03-01",
              "regfDateTo": "2024-03-31",
              "includeAdult": "true",
              "includeYouth": "false",
              "includeCompany": "y",
              "onlyAccountsWithParentGuardian": "n",
              "collectionOrderChoice": "with",
              "accountStatus": "live",
              "minBalance": 10.5,
              "maxBalance": "1000.75",
              "firstPaymentOrPaybyInNext7Days": "yes",
              "lowerNameRange": "a",
              "upperNameRange": "z"
            }""";

        ReportInstanceEntity reportInstance = new ReportInstanceEntity();
        reportInstance.setReportParameters(json);

        DefendantAccountEntity a1 = new DefendantAccountEntity();
        DefendantAccountEntity a2 = new DefendantAccountEntity();

        List<DefendantAccountEntity> accounts = Arrays.asList(a1, a2);

        when(defendantAccountRepository.findAll(
            ArgumentMatchers.<Specification<DefendantAccountEntity>>any(),
            ArgumentMatchers.<Sort>any()
        )).thenReturn(accounts);

        OperationReportByEnforcementTransaction expected = new OperationReportByEnforcementTransaction();
        expected.setTransactionList(Collections.emptyList());
        when(resultMapper.map(accounts)).thenReturn(expected);

        ReportDataInterface actual = service.generateReportData(reportInstance);

        assertThat(actual).isSameAs(expected);
    }
}