package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.defendant.DefendantAccountCore;

import java.util.List;

@Repository
public interface DefendantAccountCoreRepository extends JpaRepository<DefendantAccountCore, Long>,
        JpaSpecificationExecutor<DefendantAccountCore> {


    DefendantAccountCore findByBusinessUnit_BusinessUnitIdAndAccountNumber(Short businessUnitId,
                                                                             String accountNumber);

    List<DefendantAccountCore> findAllByBusinessUnit_BusinessUnitId(Short businessUnitId);
}
