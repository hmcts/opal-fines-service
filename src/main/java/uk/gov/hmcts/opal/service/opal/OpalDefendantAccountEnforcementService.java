package uk.gov.hmcts.opal.service.opal;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.dto.AddDefendantAccountEnforcementRequest;
import uk.gov.hmcts.opal.dto.AddEnforcementResponse;
import uk.gov.hmcts.opal.dto.EnforcementStatus;
import uk.gov.hmcts.opal.dto.common.EnforcementOverride;
import uk.gov.hmcts.opal.entity.DebtorDetailEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.entity.EnforcerEntity;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity;
import uk.gov.hmcts.opal.entity.result.ResultEntity;
import uk.gov.hmcts.opal.repository.DebtorDetailRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.repository.EnforcementRepository;
import uk.gov.hmcts.opal.repository.EnforcerRepository;
import uk.gov.hmcts.opal.repository.LocalJusticeAreaRepository;
import uk.gov.hmcts.opal.repository.ResultRepository;
import uk.gov.hmcts.opal.service.iface.DefendantAccountEnforcementServiceInterface;

import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.opal.service.opal.OpalDefendantAccountBuilders.buildEnforcementAction;
import static uk.gov.hmcts.opal.service.opal.OpalDefendantAccountBuilders.buildEnforcementOverrideResult;
import static uk.gov.hmcts.opal.service.opal.OpalDefendantAccountBuilders.buildEnforcementStatus;
import static uk.gov.hmcts.opal.service.opal.OpalDefendantAccountBuilders.filterDefendantParty;

@Service
@Slf4j(topic = "opal.OpalDefendantAccountService")
@RequiredArgsConstructor
public class OpalDefendantAccountEnforcementService
    implements DefendantAccountEnforcementServiceInterface {

    private final DefendantAccountRepository defendantAccountRepository;

    private final LocalJusticeAreaRepository localJusticeAreaRepository;

    private final EnforcerRepository enforcerRepository;

    private final EnforcementRepository enforcementRepository;

    private final DebtorDetailRepository debtorDetailRepository;

    private final ResultRepository resultRepository;

    @Transactional(readOnly = true)
    public DefendantAccountEntity getDefendantAccountById(long defendantAccountId) {
        return defendantAccountRepository
            .findById(defendantAccountId)
            .orElseThrow(
                () ->
                    new EntityNotFoundException(
                        "Defendant Account not found with id: " + defendantAccountId));
    }

    @Override
    public AddEnforcementResponse addEnforcement(
        Long defendantAccountId,
        String businessUnitId,
        String businessUnitUserId,
        String ifMatch,
        String authHeader,
        AddDefendantAccountEnforcementRequest request) {
        return null;
    }

    EnforcementOverride buildEnforcementOverride(DefendantAccountEntity entity) {
        if (entity.getEnforcementOverrideResultId() == null
            && entity.getEnforcementOverrideEnforcerId() == null
            && entity.getEnforcementOverrideTfoLjaId() == null) {
            return null;
        } else {
            return EnforcementOverride.builder()
                .enforcementOverrideResult(
                    buildEnforcementOverrideResult(
                        dbResultEntity(entity.getEnforcementOverrideResultId())))
                .enforcer(OpalDefendantAccountBuilders.buildEnforcer(dbEnforcerEntity(entity)))
                .lja(OpalDefendantAccountBuilders.buildLja(dbLja(entity)))
                .build();
        }
    }

    // These 'DB' methods are focused purely on fetching relevant entities from the DB without any
    // mapping.

    @Transactional(readOnly = true)
    Optional<ResultEntity.Lite> dbResultEntity(String resultId) {
        return Optional.ofNullable(resultId).flatMap(resultRepository::findById);
    }

    @Transactional(readOnly = true)
    Optional<EnforcerEntity> dbEnforcerEntity(DefendantAccountEntity entity) {
        return Optional.ofNullable(entity.getEnforcementOverrideEnforcerId())
            .flatMap(enforcerRepository::findById)
            .filter(enf -> Objects.nonNull(enf.getEnforcerId()));
    }

    @Transactional(readOnly = true)
    Optional<LocalJusticeAreaEntity> dbLja(DefendantAccountEntity entity) {
        return Optional.ofNullable(entity.getEnforcementOverrideTfoLjaId())
            .flatMap(localJusticeAreaRepository::findById);
    }

    @Transactional(readOnly = true)
    Optional<DebtorDetailEntity> dbDebtorDetails(PartyEntity party) {
        return debtorDetailRepository.findByPartyId(party.getPartyId());
    }

    @Transactional(readOnly = true)
    Optional<EnforcementEntity.Lite> dbEnforcementMostRecent(DefendantAccountEntity entity) {
        return enforcementRepository.findFirstByDefendantAccountIdAndResultIdOrderByPostedDateDesc(
            entity.getDefendantAccountId(), entity.getLastEnforcement());
    }

    @Override
    @Transactional(readOnly = true)
    public EnforcementStatus getEnforcementStatus(Long defendantAccountId) {

        log.debug(":getEnforcementStatus: def acc: {}", defendantAccountId);

        DefendantAccountEntity defendantEntity = getDefendantAccountById(defendantAccountId);
        DefendantAccountPartiesEntity defendantParty = filterDefendantParty(defendantEntity);
        Optional<EnforcementEntity.Lite> recentEnforcement = dbEnforcementMostRecent(defendantEntity);

        return buildEnforcementStatus(
            defendantEntity,
            defendantParty,
            dbDebtorDetails(defendantParty.getParty()),
            recentEnforcement.map(EnforcementEntity::getResult),
            buildEnforcementOverride(defendantEntity),
            buildEnforcementAction(
                recentEnforcement,
                recentEnforcement
                    .map(EnforcementEntity::getEnforcerId)
                    .map(enforcerRepository::findByEnforcerId)));
    }
}
