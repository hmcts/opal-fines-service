package uk.gov.hmcts.opal.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.alternatepaymentreference.AlternatePaymentReferenceEntity;

@Repository
public interface AlternatePaymentReferenceRepository extends JpaRepository<AlternatePaymentReferenceEntity, Long>,
        JpaSpecificationExecutor<AlternatePaymentReferenceEntity> {

    Optional<AlternatePaymentReferenceEntity> findByAprText(String aprText);
}
