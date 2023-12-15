package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountSummary;

import java.util.List;

@Repository
public interface DefendantAccountRepository extends JpaRepository<DefendantAccountEntity, Long> {

    DefendantAccountEntity findByBusinessUnitId_BusinessUnitIdAndAccountNumber(Short businessUnitId,
                                                                               String accountNumber);

    List<DefendantAccountEntity> findAllByBusinessUnitId_BusinessUnitId(Short businessUnitId);

    List<DefendantAccountSummary> findByOriginatorNameContaining(String surname);

}


