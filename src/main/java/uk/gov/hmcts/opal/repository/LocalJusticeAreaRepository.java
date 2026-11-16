package uk.gov.hmcts.opal.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.dto.DraftAccountMapping;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity;

@Repository
public interface LocalJusticeAreaRepository extends JpaRepository<LocalJusticeAreaEntity, Short>,
    JpaSpecificationExecutor<LocalJusticeAreaEntity> {

    @Transactional(readOnly = true)
    @Query(value = """
        SELECT business_unit_id, enforcing_court_id
        FROM v_lookup_lja_bu_enf_court
        WHERE local_justice_area_id = :ljaCode
        """, nativeQuery = true)
    Optional<DraftAccountMapping> findDraftAccountMappingByLjaCode(@Param("ljaCode") Short ljaCode);
}
