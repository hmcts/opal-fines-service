package uk.gov.hmcts.opal.service.report.operation.mapper;

import static uk.gov.hmcts.opal.entity.defendantaccount.AssociationType.DEFENDANT;
import static uk.gov.hmcts.opal.entity.defendantaccount.AssociationType.PARENT_GUARDIAN;

import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.PdplIdentifierType;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.debtordetail.DebtorDetailEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.service.persistence.DebtorDetailRepositoryService;
import uk.gov.hmcts.opal.service.report.CommonReportStringConstants;
import uk.gov.hmcts.opal.service.report.ReportMetadataContext;

@Service
@RequiredArgsConstructor
public class CommonRowMappingHelper {

    private final DebtorDetailRepositoryService debtorService;

    public PartyEntity pickPrimaryParty(DefendantAccountEntity account) {
        if (account == null || account.getParties() == null) {
            return null;
        }
        PartyEntity debtorFallback = null;
        PartyEntity anyFallback = null;

        for (DefendantAccountPartiesEntity link : account.getParties()) {
            if (link == null) {
                continue;
            }
            if (DEFENDANT.equals(link.getAssociationType())) {
                return link.getParty();
            }
            if (debtorFallback == null && Boolean.TRUE.equals(link.getDebtor())) {
                debtorFallback = link.getParty();
            }
            if (anyFallback == null && link.getParty() != null) {
                anyFallback = link.getParty();
            }
        }

        return debtorFallback != null ? debtorFallback : anyFallback;
    }

    public <T> void applyParty(
        DefendantAccountEntity entity,
        T dto,
        ReportMetadataContext context,
        BiConsumer<PartyEntity, T> mapParty,
        BiConsumer<DebtorDetailEntity, T> mapDebtor
    ) {
        PartyEntity party = pickPrimaryParty(entity);
        if (party == null) {
            return;
        }
        mapParty.accept(party, dto);
        Long partyId = party.getPartyId();
        if (partyId != null) {
            debtorService.findByPartyId(partyId)
                .ifPresent(debtor -> {
                    context.addParticipant(
                        String.valueOf(debtor.getPartyId()),
                        PdplIdentifierType.DEBTOR_ACCOUNT
                    );
                    mapDebtor.accept(debtor, dto);
                });
        }
    }

    public boolean applyParentGuardian(
        DefendantAccountEntity entity,
        BiConsumer<String, PdplIdentifierType> participantAdder
    ) {
        if (entity.getParties() == null) {
            return false;
        }

        List<Long> parentGuardianIds = entity.getParties().stream()
            .filter(Objects::nonNull)
            .filter(p -> PARENT_GUARDIAN.equals(p.getAssociationType()))
            .map(DefendantAccountPartiesEntity::getDefendantAccountPartyId)
            .toList();
        for (Long parentGuardianId : parentGuardianIds) {
            participantAdder.accept(String.valueOf(parentGuardianId), PdplIdentifierType.PARENT_GUARDIAN);
        }
        return !parentGuardianIds.isEmpty();
    }

    public String parentGuardianValue(DefendantAccountEntity entity) {
        if (entity.getParties() == null) {
            return null;
        }
        boolean hasParentGuardian = entity.getParties().stream()
            .filter(Objects::nonNull)
            .anyMatch(p -> PARENT_GUARDIAN.equals(p.getAssociationType()));
        return hasParentGuardian ? CommonReportStringConstants.YES : CommonReportStringConstants.NO;
    }
}