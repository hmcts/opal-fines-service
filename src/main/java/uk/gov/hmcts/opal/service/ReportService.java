package uk.gov.hmcts.opal.service;

import jakarta.persistence.EntityNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.common.service.AbstractPermissionService;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.entity.ReportEntity;
import uk.gov.hmcts.opal.entity.configurationitem.ConfigurationItemEntity;
import uk.gov.hmcts.opal.generated.model.ReportReports;
import uk.gov.hmcts.opal.mapper.ReportEntityMapper;
import uk.gov.hmcts.opal.repository.ConfigurationItemRepository;
import uk.gov.hmcts.opal.repository.ReportRepository;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.ReportService")
public class ReportService extends AbstractPermissionService {

    private static final String OPERATIONAL_REPORT_BU_WARNING_THRESHOLD = "OPERATIONAL_REPORT_BU_WARNING_THRESHOLD";
    private static final String BUSINESS_UNIT_WARNING_THRESHOLD = "business_unit_warning_threshold";
    private static final Set<String> REPORTS_WITH_BU_WARNING_THRESHOLD = Set.of(
        "operational_report_enforcement",
        "operational_report_payment"
    );

    private final ReportRepository reportRepository;
    private final ConfigurationItemRepository configurationItemRepository;
    private final ReportEntityMapper reportMapper;
    private final UserStateService userStateService;

    @Transactional(readOnly = true)
    public ReportReports getReport(String reportId) {
        log.debug(":getReport: reportId={}", reportId);

        UserState userState = userStateService.checkForAuthorisedUser();
        ReportEntity entity = reportRepository.findById(reportId)
            .orElseThrow(() -> new EntityNotFoundException("Report not found with id: " + reportId));
        checkPermission(userState, entity.getPermission());

        ReportReports report = reportMapper.toDto(entity);

        if (REPORTS_WITH_BU_WARNING_THRESHOLD.contains(reportId)) {
            report.setReportParameters(getReportParametersWithBuWarningThreshold(report.getReportParameters()));
        }

        return report;
    }

    private Map<String, Object> getReportParametersWithBuWarningThreshold(Map<String, Object> reportParameters) {
        Map<String, Object> enrichedParameters = new HashMap<>();
        if (reportParameters != null) {
            enrichedParameters.putAll(reportParameters);
        }

        ConfigurationItemEntity configurationItem = configurationItemRepository
            .findByItemNameAndBusinessUnitIdIsNull(OPERATIONAL_REPORT_BU_WARNING_THRESHOLD)
            .orElseThrow(() -> new IllegalStateException(
                "Missing configuration item: " + OPERATIONAL_REPORT_BU_WARNING_THRESHOLD
            ));

        try {
            enrichedParameters.put(BUSINESS_UNIT_WARNING_THRESHOLD, Integer.parseInt(configurationItem.getItemValue()));
        } catch (NumberFormatException e) {
            throw new IllegalStateException(
                "Invalid integer configuration item: " + OPERATIONAL_REPORT_BU_WARNING_THRESHOLD,
                e
            );
        }

        return enrichedParameters;
    }
}
