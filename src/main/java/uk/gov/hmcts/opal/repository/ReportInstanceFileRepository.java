package uk.gov.hmcts.opal.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.ReportInstanceFileEntity;
import uk.gov.hmcts.opal.entity.ReportInstanceFileEntityId;
import uk.gov.hmcts.opal.entity.report.SupportedFileType;

@Repository
public interface ReportInstanceFileRepository extends
    JpaRepository<ReportInstanceFileEntity, ReportInstanceFileEntityId> {

    Optional<ReportInstanceFileEntity> findByReportInstanceIdAndFileType(Long reportInstanceId,
        SupportedFileType fileType);
}
