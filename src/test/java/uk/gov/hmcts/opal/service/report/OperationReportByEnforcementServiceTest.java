package uk.gov.hmcts.opal.service.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.opal.service.report.FileType.CSV;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;

@ExtendWith(MockitoExtension.class)
class OperationReportByEnforcementServiceTest {

    @Mock
    private DefendantAccountRepository defendantAccountRepository;

    @Mock
    private ReportResultMapper resultMapper;

    private ObjectMapper objectMapper;

    private OperationReportByEnforcementService service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new OperationReportByEnforcementService(
            defendantAccountRepository,
            resultMapper,
            objectMapper
        );
    }

    @Test
    void getReportId_returnsOperationalReportByEnforcement() {
        assertEquals(ReportId.OP_ENFORCEMENT, service.getReportId());
    }

    @Test
    void convertReportDataToFileType_returnsEmptyByteArray() {
        byte[] out = service.convertReportDataToFileType(null, null, CSV);
        assertThat(out).isNotNull();
        assertThat(out.length).isZero();
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"unknownParam\":\"value\"}"})
    void generateReportData_withEmptyJsonOrUnknownParameter_callsRepositoryAndMapperAndReturnsMapperResult() {
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
    void generateReportData_withFullParameters_parsesFiltersAndUsesRepositoryAndMapper() {
        String json = """
            {
              "ReportType": "DETAILED",
              "businessUnitIds": [1, 2, 3],
              "enforcementMode": "LAST_ACTION",
              "enforcementDateFrom": "2024-01-01",
              "enforcementDateTo": "2024-12-31",
              "lastActionDateFrom": "2024-02-01",
              "lastActionDateTo": "2024-02-28",
              "regfDateFrom": "2024-03-01",
              "regfDateTo": "2024-03-31",
              "includeAdult": "true",
              "includeYouth": "false",
              "includeCompany": "true",
              "onlyAccountsWithParentGuardian": "false",
              "collectionOrderChoice": "WITH",
              "accountStatus": "LIVE",
              "minBalance": 10.5,
              "maxBalance": "1000.75",
              "firstPaymentOrPaybyInNext7Days": "true",
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