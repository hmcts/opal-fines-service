package uk.gov.hmcts.opal.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.InterfaceJobProcessedFileSummaryEntity;

@Repository
public interface InterfaceJobsProcessedFileSummaryRepository
    extends JpaRepository<InterfaceJobProcessedFileSummaryEntity, Long> {

    List<InterfaceJobProcessedFileSummaryEntity> findAllByInterfaceJobIdOrderByInterfaceFileIdAsc(Long interfaceJobId);
}
