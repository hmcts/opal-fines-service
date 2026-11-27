package uk.gov.hmcts.opal.repository;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.MiscellaneousAccountEntity;

@Repository
public interface MiscellaneousAccountRepository extends JpaRepository<MiscellaneousAccountEntity, Long> {

    List<MiscellaneousAccountEntity> findAllByMiscellaneousAccountIdIn(List<Long> miscellaneousAccountIds);

    @EntityGraph(attributePaths = "party")
    List<MiscellaneousAccountEntity> findWithPartyByMiscellaneousAccountIdIn(List<Long> miscellaneousAccountIds);

    @Query(value = """
        SELECT f_get_account_number(
            CAST(:businessUnitId AS smallint),
            CAST('miscellaneous_accounts' AS t_associated_record_type_enum))
        """, nativeQuery = true)
    String getNextAccountNumber(@Param("businessUnitId") Short businessUnitId);
}
