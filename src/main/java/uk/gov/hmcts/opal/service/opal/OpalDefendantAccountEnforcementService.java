package uk.gov.hmcts.opal.service.opal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.dto.AddDefendantAccountEnforcementRequest;
import uk.gov.hmcts.opal.dto.AddEnforcementResponse;
import uk.gov.hmcts.opal.dto.RemoveDefendantAccountEnforcementHoldRequest;
import uk.gov.hmcts.opal.dto.RemoveDefendantAccountEnforcementHoldResponse;
import uk.gov.hmcts.opal.dto.EnforcementStatus;
import uk.gov.hmcts.opal.dto.common.EnforcementOverride;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity;
import uk.gov.hmcts.opal.service.iface.DefendantAccountEnforcementServiceInterface;
import uk.gov.hmcts.opal.service.persistence.DebtorDetailRepositoryService;
import uk.gov.hmcts.opal.service.persistence.DefendantAccountRepositoryService;
import uk.gov.hmcts.opal.service.persistence.EnforcementRepositoryService;
import uk.gov.hmcts.opal.service.persistence.EnforcerRepositoryService;
import uk.gov.hmcts.opal.service.persistence.LocalJusticeAreaRepositoryService;
import uk.gov.hmcts.opal.service.persistence.ResultRepositoryService;

import static uk.gov.hmcts.opal.service.opal.OpalDefendantAccountBuilders.buildEnforcementAction;
import static uk.gov.hmcts.opal.service.opal.OpalDefendantAccountBuilders.buildEnforcementOverrideResult;
import static uk.gov.hmcts.opal.service.opal.OpalDefendantAccountBuilders.buildEnforcementStatus;
import static uk.gov.hmcts.opal.service.opal.OpalDefendantAccountBuilders.filterDefendantParty;

@Service
@Slf4j(topic = "opal.OpalDefendantAccountService")
@RequiredArgsConstructor
public class OpalDefendantAccountEnforcementService
    implements DefendantAccountEnforcementServiceInterface {

    private final DefendantAccountRepositoryService defendantAccountRepositoryService;

    private final LocalJusticeAreaRepositoryService localJusticeAreaRepositoryService;

    private final EnforcerRepositoryService enforcerRepositoryService;

    private final EnforcementRepositoryService enforcementRepositoryService;

    private final DebtorDetailRepositoryService debtorDetailRepositoryService;

    private final ResultRepositoryService resultRepositoryService;

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

    @Override
    public RemoveDefendantAccountEnforcementHoldResponse removeEnforcementHold(
        Long defendantAccountId,
        Short businessUnitId,
        String businessUnitUserId,
        String ifMatch,
        String authHeader,
        RemoveDefendantAccountEnforcementHoldRequest request) {
        throw new UnsupportedOperationException("Remove enforcement hold not yet implemented for OPAL mode");
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
                        resultRepositoryService.getResultById(entity.getEnforcementOverrideResultId()).orElse(null)))
                .enforcer(OpalDefendantAccountBuilders.buildEnforcer(
                    enforcerRepositoryService.findById(entity.getEnforcementOverrideEnforcerId()).orElse(null)))
                .lja(OpalDefendantAccountBuilders.buildLja(
                    localJusticeAreaRepositoryService.getLjaById(entity.getEnforcementOverrideTfoLjaId()).orElse(null)))
                .build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public EnforcementStatus getEnforcementStatus(Long defendantAccountId) {

        log.debug(":getEnforcementStatus: def acc: {}", defendantAccountId);

        DefendantAccountEntity defendantEntity = defendantAccountRepositoryService
            .findById(defendantAccountId);
        DefendantAccountPartiesEntity defendantParty = filterDefendantParty(defendantEntity);
        EnforcementEntity recentEnforcement =
            enforcementRepositoryService.getEnforcementMostRecent(
                defendantEntity.getDefendantAccountId(), defendantEntity.getLastEnforcement()).orElse(null);

        return buildEnforcementStatus(
            defendantEntity,
            defendantParty,
            debtorDetailRepositoryService.findByPartyId(defendantParty.getParty().getPartyId()).orElse(null),
            recentEnforcement != null ? recentEnforcement.getResult() : null,
            buildEnforcementOverride(defendantEntity),
            buildEnforcementAction(
                recentEnforcement,
                recentEnforcement != null
                    ? enforcerRepositoryService.findById(recentEnforcement.getEnforcerId()).orElse(null)
                    : null));
    }
}
