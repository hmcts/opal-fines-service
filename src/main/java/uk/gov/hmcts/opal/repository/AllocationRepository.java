package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.AllocationEntity;

@Repository
public interface AllocationRepository extends JpaRepository<AllocationEntity, Long> {

    void deleteByDefendantTransaction_DefendantAccount_DefendantAccountId(Long defendantAccountId);

    void deleteByImposition_DefendantAccount_DefendantAccountId(Long defendantAccountId);
}
