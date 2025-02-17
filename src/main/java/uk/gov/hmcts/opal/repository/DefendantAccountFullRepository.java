package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.defendant.DefendantAccountFull;

import java.util.List;

@Repository
public interface DefendantAccountFullRepository extends JpaRepository<DefendantAccountFull, Long>,
        JpaSpecificationExecutor<DefendantAccountFull> {


    DefendantAccountFull findByBusinessUnit_BusinessUnitIdAndAccountNumber(Short businessUnitId,
                                                                             String accountNumber);

    List<DefendantAccountFull> findAllByBusinessUnit_BusinessUnitId(Short businessUnitId);
}
