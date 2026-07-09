package uk.gov.hmcts.opal.service.report;

import static uk.gov.hmcts.opal.entity.report.ReportInstanceGenerationStatus.REQUESTED;
import static uk.gov.hmcts.opal.service.report.ReportId.CASH_TILL;

import jakarta.persistence.EntityNotFoundException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.entity.ReportEntity;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.entity.TillEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.exception.EntityNotSavedException;
import uk.gov.hmcts.opal.repository.ReportInstanceRepository;
import uk.gov.hmcts.opal.repository.ReportRepository;
import uk.gov.hmcts.opal.repository.TillRepository;
import uk.gov.hmcts.opal.service.UserStateService;

@Service
@RequiredArgsConstructor
public class PreAllocatedCashTillService {

    private static final String PRE_ALLOCATED_REPORT_NAME = "Cash till report - Pre-allocated (%s)";

    private final Clock clock;
    private final ObjectMapper objectMapper;
    private final ReportInstanceRepository reportInstanceRepository;
    private final ReportRepository reportRepository;
    private final TillRepository tillRepository;
    private final UserStateService userStateService;
    private final GenericReportService genericReportService;

    @Transactional
    public Long createPreAllocatedReportInstance(Long tillId) {
        TillEntity till = findTill(tillId);
        ReportEntity report = reportRepository.findById(CASH_TILL.getReportId())
            .orElseThrow(() -> new EntityNotFoundException("Report not found with id: " + CASH_TILL.getReportId()));
        UserState userState = userStateService.getUserStateV1FromSecurityContext();

        ReportInstanceEntity reportInstance = ReportInstanceEntity.builder()
            .report(report)
            .businessUnit(List.of(getBusinessUnit(till, tillId).getBusinessUnitId()))
            .requestedBy(userState.getUserId())
            .requestedByName(userState.getUserName())
            .reportParameters(toReportParameters(tillId))
            .requestedAt(LocalDateTime.now(clock))
            .generationStatus(REQUESTED)
            .reportName(PRE_ALLOCATED_REPORT_NAME.formatted(till.getTillNumber()))
            .build();

        ReportInstanceEntity savedReportInstance = saveReportInstance(reportInstance);
        genericReportService.generateReportInstanceContent(savedReportInstance.getReportInstanceId());
        return savedReportInstance.getReportInstanceId();
    }

    private TillEntity findTill(Long tillId) {
        if (tillId == null || tillId <= 0) {
            throw new IllegalArgumentException("Cash Till report till_id is required");
        }
        return tillRepository.findById(tillId)
            .orElseThrow(() -> new EntityNotFoundException("Cash Till report till not found for till_id " + tillId));
    }

    private static BusinessUnitEntity getBusinessUnit(TillEntity till, Long tillId) {
        BusinessUnitEntity businessUnit = till.getBusinessUnit();
        if (businessUnit == null) {
            throw new EntityNotFoundException("Cash Till report business unit not found for till_id " + tillId);
        }
        return businessUnit;
    }

    private String toReportParameters(Long tillId) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                "till_id", tillId,
                "allocated_report", false));
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Report parameters badly formatted", e);
        }
    }

    private ReportInstanceEntity saveReportInstance(ReportInstanceEntity reportInstance) {
        try {
            return reportInstanceRepository.save(reportInstance);
        } catch (Exception e) {
            throw new EntityNotSavedException("Unable to save report instance", e);
        }
    }
}
