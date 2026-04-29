package uk.gov.hmcts.opal.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.entity.ReportEntity;
import uk.gov.hmcts.opal.generated.model.ReportReports;
import uk.gov.hmcts.opal.mapper.ReportMapper;
import uk.gov.hmcts.opal.repository.ReportRepository;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.ReportService")
public class ReportService {

    private final ReportRepository reportRepository;
    private final ReportMapper reportMapper;

    /**
     * Get report definition by ID.
     *
     * @param reportId the report ID
     * @return the report DTO
     * @throws EntityNotFoundException if the report is not found
     */
    @Transactional(readOnly = true)
    public ReportReports getReport(String reportId) {
        log.debug(":getReport: reportId={}", reportId);

        ReportEntity entity = reportRepository.findById(reportId)
            .orElseThrow(() -> new EntityNotFoundException("Report not found with id: " + reportId));

        return reportMapper.toDto(entity);
    }
}

