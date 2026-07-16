package uk.gov.hmcts.opal.service.report;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.service.report.mapper.csv.ReportCSVMapper;

@Component
public class ReportCSVMapperRegistry {

    private final Map<Class<? extends ReportDataInterface>, ReportCSVMapper<?>> mappers;

    public ReportCSVMapperRegistry(List<ReportCSVMapper<?>> mappers) {
        this.mappers = mappers.stream()
            .collect(Collectors.toMap(
                ReportCSVMapper::getReportDataType,
                Function.identity()
            ));
    }

    @SuppressWarnings("unchecked")
    public <T extends ReportDataInterface> ReportCSVMapper<T> get(Class<T> reportType) {
        return (ReportCSVMapper<T>) mappers.get(reportType);
    }
}
