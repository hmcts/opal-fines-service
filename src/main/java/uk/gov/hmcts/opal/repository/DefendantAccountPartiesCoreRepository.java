package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.defendant.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.entity.defendant.DefendantAccountPartiesEntityCore;

@Repository
public interface DefendantAccountPartiesCoreRepository extends JpaRepository<DefendantAccountPartiesEntity, Long> {


    DefendantAccountPartiesEntityCore findByDefendantAccount_DefendantAccountId(Long defendantAccountId);
}
