package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.DefendantAccountPartiesEntity;

@Repository
public interface DefendantAccountPartiesRepository extends JpaRepository<DefendantAccountPartiesEntity, Long> {


    DefendantAccountPartiesEntity findByDefendantAccount_DefendantAccountId(Long defendantAccountId);

    void deleteByDefendantAccount_DefendantAccountId(long defendantAccountId);
}
