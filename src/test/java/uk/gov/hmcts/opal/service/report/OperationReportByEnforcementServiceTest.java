package uk.gov.hmcts.opal.service.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import groovy.io.FileType;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
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
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.util.JsonPathUtil;
import uk.gov.hmcts.opal.util.JsonPathUtil.DocContext;

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
        byte[] out = service.convertReportDataToFileType(null, null, FileType.FILES);
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
        String json = "{\n"
            + "  \"Report_types\": \"Detailed\",\n"
            + "  \"business_unit_ids\": [1, 2, 3],\n"
            + "  \"enforcementMode\": \"LAST_ACTION\",\n"
            + "  \"enforcementDateFrom\": \"2024-01-01\",\n"
            + "  \"enforcementDateTo\": \"2024-12-31\",\n"
            + "  \"lastActionDateFrom\": \"2024-02-01\",\n"
            + "  \"lastActionDateTo\": \"2024-02-28\",\n"
            + "  \"regfDateFrom\": \"2024-03-01\",\n"
            + "  \"regfDateTo\": \"2024-03-31\",\n"
            + "  \"includeAdult\": \"true\",\n"
            + "  \"includeYouth\": \"false\",\n"
            + "  \"includeCompany\": \"y\",\n"
            + "  \"onlyAccountsWithParentGuardian\": \"n\",\n"
            + "  \"collectionOrderChoice\": \"with\",\n"
            + "  \"accountStatus\": \"live\",\n"
            + "  \"minBalance\": 10.5,\n"
            + "  \"maxBalance\": \"1000.75\",\n"
            + "  \"firstPaymentOrPaybyInNext7Days\": \"yes\",\n"
            + "  \"lowerNameRange\": \"a\",\n"
            + "  \"upperNameRange\": \"z\"\n"
            + "}";

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

    @Test
    void safeReadString_returnsDefaultOnException_orNulls_and_normalValues_when_present() throws Exception {
        DocContext ctx = JsonPathUtil.createDocContext("{\"a\": \"value\", \"b\": null}", "");
        Method safeReadString = OperationReportByEnforcementService.class
            .getDeclaredMethod("safeReadString", DocContext.class, String.class, String.class);
        safeReadString.setAccessible(true);

        Object r1 = safeReadString.invoke(null, ctx, "$.a", "def");
        assertThat(r1).isEqualTo("value");

        Object r2 = safeReadString.invoke(null, ctx, "$.b", "defB");
        assertThat(r2).isEqualTo("defB");

        Object r3 = safeReadString.invoke(null, ctx, "$.missing", "x");
        assertThat(r3).isEqualTo("x");
    }

    @Test
    void safeReadLocalDate_parses_dates_and_handles_invalids() throws Exception {
        DocContext ctx = JsonPathUtil.createDocContext("{\"d1\":\"2024-01-02\",\"d2\":\"\",\"d3\":\"not-a-date\"}", "");
        Method m = OperationReportByEnforcementService.class
            .getDeclaredMethod("safeReadLocalDate", DocContext.class, String.class);
        m.setAccessible(true);

        Object o1 = m.invoke(null, ctx, "$.d1");
        assertThat(o1).isEqualTo(LocalDate.parse("2024-01-02"));

        Object o2 = m.invoke(null, ctx, "$.d2");
        assertThat(o2).isNull();

        Object o3 = m.invoke(null, ctx, "$.d3");
        assertThat(o3).isNull();

        Object o4 = m.invoke(null, ctx, "$.doesNotExist");
        assertThat(o4).isNull();
    }

    @Test
    void safeReadBoolean_parses_variations_and_defaults() throws Exception {
        DocContext ctx = JsonPathUtil.createDocContext("{"
            + "\"t1\": true, \"t2\":\"true\", \"t3\":\"y\", \"t4\":\"yes\","
            + "\"f1\": false, \"f2\":\"false\", \"f3\":\"n\", \"f4\":\"no\","
            + "\"garbage\":\"maybe\" }", "");

        Method m = OperationReportByEnforcementService.class
            .getDeclaredMethod("safeReadBoolean", DocContext.class, String.class, Boolean.class);
        m.setAccessible(true);

        Object r1 = m.invoke(null, ctx, "$.t1", Boolean.FALSE);
        Object r2 = m.invoke(null, ctx, "$.t2", Boolean.FALSE);
        Object r3 = m.invoke(null, ctx, "$.t3", Boolean.FALSE);
        Object r4 = m.invoke(null, ctx, "$.t4", Boolean.FALSE);

        Object f1 = m.invoke(null, ctx, "$.f1", Boolean.TRUE);
        Object f2 = m.invoke(null, ctx, "$.f2", Boolean.TRUE);
        Object f3 = m.invoke(null, ctx, "$.f3", Boolean.TRUE);
        Object f4 = m.invoke(null, ctx, "$.f4", Boolean.TRUE);

        Object g = m.invoke(null, ctx, "$.garbage", Boolean.TRUE);
        Object miss = m.invoke(null, ctx, "$.doesNotExist", Boolean.TRUE);

        assertThat(r1).isEqualTo(Boolean.TRUE);
        assertThat(r2).isEqualTo(Boolean.TRUE);
        assertThat(r3).isEqualTo(Boolean.TRUE);
        assertThat(r4).isEqualTo(Boolean.TRUE);

        assertThat(f1).isEqualTo(Boolean.FALSE);
        assertThat(f2).isEqualTo(Boolean.FALSE);
        assertThat(f3).isEqualTo(Boolean.FALSE);
        assertThat(f4).isEqualTo(Boolean.FALSE);

        assertThat(g).isEqualTo(Boolean.TRUE);
        assertThat(miss).isEqualTo(Boolean.TRUE);
    }

    @Test
    void safeReadBigDecimal_handles_numbers_and_strings_and_invalid() throws Exception {
        DocContext ctx = JsonPathUtil.createDocContext("{\"n1\": 12.34, \"n2\": \"56.78\", \"bad\": \"x\"}", "");
        Method m = OperationReportByEnforcementService.class
            .getDeclaredMethod("safeReadBigDecimal", DocContext.class, String.class);
        m.setAccessible(true);

        Object o1 = m.invoke(null, ctx, "$.n1");
        assertThat(o1).isInstanceOf(BigDecimal.class);
        assertThat(((BigDecimal) o1).compareTo(new BigDecimal("12.34"))).isZero();

        Object o2 = m.invoke(null, ctx, "$.n2");
        assertThat(o2).isInstanceOf(BigDecimal.class);
        assertThat(((BigDecimal) o2).compareTo(new BigDecimal("56.78"))).isZero();

        Object o3 = m.invoke(null, ctx, "$.bad");
        assertThat(o3).isNull();

        Object o4 = m.invoke(null, ctx, "$.missing");
        assertThat(o4).isNull();
    }
}