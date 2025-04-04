package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.defendant.DefendantAccountPartiesEntity;

@Repository
public interface DefendantAccountPartiesRepository extends JpaRepository<DefendantAccountPartiesEntity, Long> {


    DefendantAccountPartiesEntity findByDefendantAccount_DefendantAccountId(Long defendantAccountId);
}
