package uk.gov.hmcts.opal.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.alternatepaymentreference.AlternatePaymentReferenceEntity;

@Repository
public interface AlternatePaymentReferenceRepository extends JpaRepository<AlternatePaymentReferenceEntity, Long>,
    JpaSpecificationExecutor<AlternatePaymentReferenceEntity> {

    Optional<AlternatePaymentReferenceEntity> findByAprTextAndBusinessUnit_businessUnitId(
        String aprText, short businessUnitId);

    List<AlternatePaymentReferenceEntity> findByDefendantAccount_defendantAccountId(Long defendantAccountId);
}
