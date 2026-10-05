package uk.gov.hmcts.opal.service.report;

import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.entity.ReportInstanceFileEntity;
import uk.gov.hmcts.opal.exception.InvalidReferenceValidationException;
import uk.gov.hmcts.opal.repository.ReportInstanceFileRepository;
import uk.gov.hmcts.opal.repository.ReportInstanceRepository;
import uk.gov.hmcts.opal.service.blobstore.ReportBlobStore;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.ReportInstanceDeletionService")
public class ReportInstanceDeletionService {

    private final ReportInstanceRepository reportInstanceRepository;
    private final ReportInstanceFileRepository reportInstanceFileEntityRepository;
    private final ReportBlobStore reportBlobStore;

    @Transactional
    public void deleteReportInstances(List<Long> reportInstanceIds) {
        if (reportInstanceIds == null || reportInstanceIds.isEmpty()
            || reportInstanceIds.stream().anyMatch(Objects::isNull)) {
            throw new InvalidReferenceValidationException("At least one report instance id must be supplied");
        }

        List<Long> distinctReportInstanceIds = reportInstanceIds.stream()
            .distinct()
            .toList();
        List<ReportInstanceEntity> reportInstances = reportInstanceRepository.findAllById(distinctReportInstanceIds);

        log.warn("DESTRUCTIVE OPERATION: Deleting report instances with ids: {}", distinctReportInstanceIds);
        reportInstances.stream()
            .map(ReportInstanceEntity::getLocation)
            .filter(Objects::nonNull)
            .distinct()
            .forEach(reportBlobStore::deleteReport);

        List<ReportInstanceFileEntity> reportInstanceFileEntities = reportInstances.stream()
            .map(ReportInstanceEntity::getReportInstanceFiles)
            .flatMap(List::stream)
            .toList();
        reportInstanceFileEntityRepository.deleteAll(reportInstanceFileEntities);
        reportInstanceRepository.deleteAll(reportInstances);
    }
}
