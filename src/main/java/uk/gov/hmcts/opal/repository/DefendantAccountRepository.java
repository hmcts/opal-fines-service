package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;

import java.util.List;

@Repository
public interface DefendantAccountRepository extends JpaRepository<DefendantAccountEntity, Long> {

    DefendantAccountEntity findByBusinessUnitIdAndAccountNumber(Short businessUnitId, String accountNumber);

    List<DefendantAccountEntity> findAllByBusinessUnitId(Short businessUnitId);

}
