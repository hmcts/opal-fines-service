package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.CommittalWarrantProgressEntity;

@Repository
public interface CommittalWarrantProgressRepository extends JpaRepository<CommittalWarrantProgressEntity, Long>,
    JpaSpecificationExecutor<CommittalWarrantProgressEntity> {

    CommittalWarrantProgressEntity findByDefendantAccountId(Long defendantAccountId);
}
