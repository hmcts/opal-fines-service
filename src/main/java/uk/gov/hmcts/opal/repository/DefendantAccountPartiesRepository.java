package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.DefendantAccountPartiesEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

@Repository
public interface DefendantAccountPartiesRepository extends JpaRepository<DefendantAccountPartiesEntity, Long> {

    @Query("""
        SELECT dap FROM DefendantAccountPartiesEntity dap
        JOIN dap.party p
        JOIN dap.defendantAccount da
        JOIN da.lastHearingCourtId c
        WHERE da.defendantAccountId = :defendantAccountId""")
    DefendantAccountPartiesEntity findByDefendantAccountId(@Param("defendantAccountId") Long defendantAccountId);
}
