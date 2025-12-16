package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.ReportEntryEntity;

@Repository
public interface ReportEntryRepository extends JpaRepository<ReportEntryEntity, Long> {

    void deleteByAssociatedRecordId(
        String associatedRecordIds
    );

    ReportEntryEntity getAllByAssociatedRecordId(String associatedRecordId);
}
