package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.defendant.DefendantAccount;

import java.util.List;

@Repository
public interface DefendantAccountRepository extends JpaRepository<DefendantAccount.Lite, Long>,
        JpaSpecificationExecutor<DefendantAccount.Lite> {

    DefendantAccount.Lite findByBusinessUnit_BusinessUnitIdAndAccountNumber(Short businessUnitId,
                                                                            String accountNumber);

    List<DefendantAccount.Lite> findAllByBusinessUnit_BusinessUnitId(Short businessUnitId);

}
