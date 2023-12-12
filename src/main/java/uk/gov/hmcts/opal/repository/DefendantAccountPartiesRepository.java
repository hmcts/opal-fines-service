package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.DefendantAccountPartiesEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface DefendantAccountPartiesRepository extends JpaRepository<DefendantAccountPartiesEntity, Long> {

    @Query("SELECT dap FROM DefendantAccountPartyEntity dap " +
        "JOIN dap.partyId p " +
        "JOIN dap.defendantAccountId da " +
        "JOIN da.lastHearingCourtId c " +
        "WHERE da.account_number = :accountNumber " +
        "AND p.dateOfBirth = :dateOfBirth " +
        "AND p.surname = :surname " +
        "AND p.forenames = :forenames " +
        "AND p.title = :title " +
        "AND da.accountBalance = :balance" +
        "AND da.lastHearingCourtId = :court")
    DefendantAccountPartiesEntity findByDefendantAccountDetailsCustomQuery(@Param("accountNumber") String accountNumber,
                                                            @Param("dateOfBirth") LocalDate dateOfBirth,
                                                            @Param("surname") String surname,
                                                            @Param("forenames") String forenames,
                                                            @Param("title") String title,
                                                            @Param("addressLine1") String addressLine1,
                                                            @Param("balance") BigDecimal balance,
                                                            @Param("court") String court);
}
