package uk.gov.hmcts.opal.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.alternatepaymentreference.AlternatePaymentReferenceEntity;
import uk.gov.hmcts.opal.entity.alternatepaymentreference.Relationship;

@Repository
public interface AlternatePaymentReferenceRepository extends JpaRepository<AlternatePaymentReferenceEntity, Long>,
    JpaSpecificationExecutor<AlternatePaymentReferenceEntity> {

    Optional<AlternatePaymentReferenceEntity> findByAprTextAndBusinessUnit_businessUnitId(
        String aprText, short businessUnitId);

    Optional<AlternatePaymentReferenceEntity> findByAlternatePaymentReferenceIdAndDefendantAccount_defendantAccountId(
        Long alternatePaymentReferenceId, Long defendantAccountId);

    List<AlternatePaymentReferenceEntity> findByDefendantAccount_defendantAccountId(Long defendantAccountId);

    boolean existsByAprTextAndBusinessUnit_businessUnitId(String aprText, short businessUnitId);

    boolean existsByDefendantAccount_defendantAccountIdAndRelationship(
        Long defendantAccountId, Relationship relationship);
}