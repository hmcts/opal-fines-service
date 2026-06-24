package uk.gov.hmcts.opal.config;

import java.util.HashMap;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.opal.service.report.ReportDataInterface;
import uk.gov.hmcts.opal.service.report.mapper.csv.OperationByEnforcementReportDetailedCSVMapper;
import uk.gov.hmcts.opal.service.report.mapper.csv.ReportCSVMapper;
import uk.gov.hmcts.opal.service.report.operationbyenforcement.OperationByEnforcementDetailedReport;

@Configuration
public class ReportConversionConfiguration {

    /**
     * Configuration for CSV report conversion. The Map Key will be the class/type of the report data, and value is the
     * ReportCSVMapper that
     * Note: The generics for each map entry should be the same concrete type
     *
     * @param operationByEnforcementReportDetailedCSVMapper
     * @return Map of
     */
    @Bean
    public Map<Class<? extends ReportDataInterface>, ReportCSVMapper<? extends ReportDataInterface>>
        reportToCSVStringMapperMap(
        OperationByEnforcementReportDetailedCSVMapper operationByEnforcementReportDetailedCSVMapper) {
        Map<Class<? extends ReportDataInterface>, ReportCSVMapper<? extends ReportDataInterface>>
            reportToCSVStringMapperMap = new HashMap<>();
        reportToCSVStringMapperMap.put(OperationByEnforcementDetailedReport.class,
            operationByEnforcementReportDetailedCSVMapper);
        //etc
        return reportToCSVStringMapperMap;
    }
}
