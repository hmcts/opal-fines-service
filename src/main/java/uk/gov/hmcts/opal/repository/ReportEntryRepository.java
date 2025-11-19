package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.ReportEntryEntity;

@Repository
public interface ReportEntryRepository extends JpaRepository<ReportEntryEntity, Long> {
    // Example: if you ever need to query active entries later
    // List<ReportEntryEntity> findByActiveTrue();

    // Example: if there is a need to find by report_id
    // Optional<ReportEntryEntity> findByReportId(Long reportId);
}
