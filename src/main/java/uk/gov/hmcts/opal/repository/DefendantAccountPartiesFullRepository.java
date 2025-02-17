package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.defendant.DefendantAccountPartiesEntityFull;

@Repository
public interface DefendantAccountPartiesFullRepository extends JpaRepository<DefendantAccountPartiesEntityFull, Long> {


    DefendantAccountPartiesEntityFull findByDefendantAccount_DefendantAccountId(Long defendantAccountId);
}
