package uk.gov.hmcts.opal.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.imposition.ImpositionEntity;
import uk.gov.hmcts.opal.entity.projection.DefendantAccountImpositionData;

@Repository
public interface ImpositionRepository extends JpaRepository<ImpositionEntity, Long>,
    JpaSpecificationExecutor<ImpositionEntity> {

    @Override
    @EntityGraph(value = ImpositionEntity.ENTITY_GRAPH_FULL, type = EntityGraph.EntityGraphType.FETCH)
    Optional<ImpositionEntity> findById(Long impositionId);

    @Override
    @EntityGraph(value = ImpositionEntity.ENTITY_GRAPH_LITE, type = EntityGraph.EntityGraphType.FETCH)
    List<ImpositionEntity> findAll(Specification<ImpositionEntity> spec);

    @EntityGraph(value = ImpositionEntity.ENTITY_GRAPH_LITE, type = EntityGraph.EntityGraphType.FETCH)
    List<ImpositionEntity> findAllByDefendantAccountId(long defendantAccountId);

    @EntityGraph(value = ImpositionEntity.ENTITY_GRAPH_LITE, type = EntityGraph.EntityGraphType.FETCH)
    ImpositionEntity findFirstByDefendantAccountIdOrderByImposedDateAsc(Long defendantAccountId);


    @Query("""
        SELECT new uk.gov.hmcts.opal.entity.projection.DefendantAccountImpositionData(
            defendantAccount.defendantAccountId,
            defendantAccount.versionNumber,
            defendantAccount.accountType,
            imposition.impositionId,
            imposition.postedDate,
            result.resultId,
            result.resultTitle,
            creditorAccount.creditorAccountId,
            creditorAccount.creditorAccountType,
            creditorAccount.majorCreditorId,
            majorCreditor.name,
            creditorAccount.minorCreditorPartyId,
            minorCreditor.organisation,
            minorCreditor.organisationName,
            minorCreditor.title,
            minorCreditor.forenames,
            minorCreditor.surname,
            imposition.imposedDate,
            imposition.imposedAmount,
            imposition.paidAmount,
            imposition.offenceId,
            imposition.offenceCode,
            imposition.offenceTitle,
            offence.cjsCode,
            offence.offenceTitle,
            imposingCourt.courtId,
            imposingCourt.courtCode,
            imposingCourt.name
        )
        FROM ImpositionEntity imposition
        JOIN imposition.defendantAccount defendantAccount
        JOIN imposition.creditorAccount creditorAccount
        JOIN ResultEntity result ON result.resultId = imposition.resultId
        LEFT JOIN creditorAccount.majorCreditor majorCreditor
        LEFT JOIN PartyEntity minorCreditor ON minorCreditor.partyId = creditorAccount.minorCreditorPartyId
        LEFT JOIN OffenceEntity offence ON offence.offenceId = imposition.offenceId
        LEFT JOIN imposition.imposingCourt imposingCourt
        WHERE imposition.defendantAccountId = :defendantAccountId
        ORDER BY imposition.postedDate, imposition.impositionId
        """)
    List<DefendantAccountImpositionData> findDefendantAccountImpositionsByDefendantAccountId(
        @Param("defendantAccountId") Long defendantAccountId
    );
}
