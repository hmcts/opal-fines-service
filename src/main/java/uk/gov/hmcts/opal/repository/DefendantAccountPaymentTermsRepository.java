package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.PaymentTermsEntity;

import java.util.Optional;

@Repository
public interface DefendantAccountPaymentTermsRepository extends JpaRepository<PaymentTermsEntity, Long>,
    JpaSpecificationExecutor<PaymentTermsEntity> {

    Optional<PaymentTermsEntity>
        findTopByDefendantAccount_DefendantAccountIdOrderByPostedDateDescPaymentTermsIdDesc(Long accountId);

    @Modifying
    @Query("UPDATE PaymentTermsEntity p "
        + "SET p.active = false "
        + "WHERE p.defendantAccount.defendantAccountId = :defAccountId "
        + "AND p.active = true")
    void deactivateActiveByDefendantAccountId(@Param("defAccountId") Long defendantAccountId);
}
