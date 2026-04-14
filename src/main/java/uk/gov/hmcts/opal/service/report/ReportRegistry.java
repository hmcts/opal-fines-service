package uk.gov.hmcts.opal.service.report;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.exception.ReportNotFoundException;

@Component
public class ReportRegistry {

    private final Map<ReportType, ReportInterface<?>> reports;

    public ReportRegistry(List<ReportInterface<?>> reports) {
        this.reports = reports.stream()
            .collect(Collectors.toMap(
                ReportInterface::getType,
                Function.identity()
            ));
    }

    public ReportInterface<?> get(String reportId) {
        return Optional.ofNullable(reports.get(ReportType.fromReportId(reportId)))
            .orElseThrow(() -> new ReportNotFoundException("No implementation found for reportId: " + reportId));

    }

}
