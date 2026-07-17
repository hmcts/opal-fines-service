package uk.gov.hmcts.opal.repository;

import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.defendantaccount.AssociationType;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountPartiesEntity;

@Repository
public interface DefendantAccountPartiesRepository extends JpaRepository<DefendantAccountPartiesEntity, Long> {


    DefendantAccountPartiesEntity findByDefendantAccount_DefendantAccountId(Long defendantAccountId);

    long countByDefendantAccount_DefendantAccountId(Long defendantAccountId);

    int countByDefendantAccount_DefendantAccountIdAndDefendantAccountPartyId(
        Long defendantAccountId, Long defendantAccountPartyId);

    int countByDefendantAccount_DefendantAccountIdAndAssociationType(
        Long defendantAccountId, AssociationType associationType);

    long countByDefendantAccount_DefendantAccountIdAndParty_OrganisationNameIn(
        Long defendantAccountId, Collection<String> organisationNames);

    @Modifying
    @Query(value = """
        DELETE FROM defendant_account_parties
        WHERE defendant_account_id = :defendantAccountId
          AND association_type = CAST(:#{#associationType.label} AS t_association_type_enum)
          AND defendant_account_party_id <> :defendantAccountPartyId
        """, nativeQuery = true)
    int deleteByAccountIdAndAssociationTypeExcludingDapId(
        @Param("defendantAccountId") Long defendantAccountId,
        @Param("associationType") AssociationType associationType,
        @Param("defendantAccountPartyId") Long defendantAccountPartyId
    );

    void deleteByDefendantAccount_DefendantAccountId(long defendantAccountId);
}
