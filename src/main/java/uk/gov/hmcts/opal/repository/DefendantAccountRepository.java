package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;

import java.util.List;

@Repository
public interface DefendantAccountRepository extends JpaRepository<DefendantAccountEntity, Long>,
        JpaSpecificationExecutor<DefendantAccountEntity> {

    DefendantAccountEntity findByBusinessUnitId_BusinessUnitIdAndAccountNumber(Short businessUnitId,
                                                                               String accountNumber);

    List<DefendantAccountEntity> findAllByBusinessUnitId_BusinessUnitId(Short businessUnitId);

}
