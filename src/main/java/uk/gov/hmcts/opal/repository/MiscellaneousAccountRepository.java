package uk.gov.hmcts.opal.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.MiscellaneousAccountEntity;

@Repository
public interface MiscellaneousAccountRepository extends JpaRepository<MiscellaneousAccountEntity, Long> {

    @Query(value = "SELECT f_get_account_number(:businessUnitId, 'miscellaneous_accounts')", nativeQuery = true)
    String nextAccountNumber(@Param("businessUnitId") Short businessUnitId);

    List<MiscellaneousAccountEntity> findAllByMiscellaneousAccountIdIn(List<Long> miscellaneousAccountIds);
}
