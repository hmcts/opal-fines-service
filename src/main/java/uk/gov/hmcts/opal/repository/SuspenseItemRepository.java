package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.SuspenseItemEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface SuspenseItemRepository extends JpaRepository<SuspenseItemEntity, Long> {

    @Query(value = """
        SELECT si.suspense_item_id FROM public.suspense_items si
        JOIN public.suspense_accounts sa
          ON sa.suspense_account_id = si.suspense_account_id
        WHERE si.suspense_item_id = :suspenseItemId
          AND sa.business_unit_id = :businessUnitId
        """, nativeQuery = true)
    Optional<Long> findItemIdForBusinessUnit(
        @Param("suspenseItemId") Long suspenseItemId,
        @Param("businessUnitId") Long businessUnitId);

}
