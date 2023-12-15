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
        WHERE da.accountNumber = :accountNumber
        AND p.dateOfBirth = :dateOfBirth
        AND p.surname = :surname
        AND p.forenames = :forenames
        AND p.title = :title
        and p.addressLine1 = :addressLine1
        AND da.accountBalance = :balance
        AND da.lastHearingCourtId = :court""")
    DefendantAccountPartiesEntity findByDefendantAccountDetailsCustomQuery(@Param("accountNumber") String accountNumber,
                                                            @Param("dateOfBirth") LocalDate dateOfBirth,
                                                            @Param("surname") String surname,
                                                            @Param("forenames") String forenames,
                                                            @Param("title") String title,
                                                            @Param("addressLine1") String addressLine1,
                                                            @Param("balance") BigDecimal balance,
                                                            @Param("court") String court);
}
