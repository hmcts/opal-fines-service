package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.ReportEntity;

@Repository
public interface ReportRepository extends JpaRepository<ReportEntity, String> {

    void deleteByReportId(String reportId);
}
