package uk.gov.hmcts.opal.mapper.report;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.opal.dto.PdplIdentifierType;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.debtordetail.DebtorDetailEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.AssociationType;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.service.persistence.DebtorDetailRepositoryService;
import uk.gov.hmcts.opal.service.persistence.EnforcementRepositoryService;
import uk.gov.hmcts.opal.dto.report.EnforcementReportRowDto;
import uk.gov.hmcts.opal.service.report.ReportMetadataContext;

public class ReportRowDtoCoreMapperDecorator implements ReportRowDtoMapper {

    @Autowired
    private ReportRowDtoCoreMapper delegate;
    @Autowired
    private DebtorDetailRepositoryService debtorService;
    @Autowired
    private EnforcementRepositoryService enforcementService;

    public void setDelegate(ReportRowDtoCoreMapper delegate) {
        this.delegate = delegate;
    }

    public void setDebtorService(DebtorDetailRepositoryService debtorService) {
        this.debtorService = debtorService;
    }

    public void setEnforcementService(EnforcementRepositoryService enforcementService) {
        this.enforcementService = enforcementService;
    }

    @Override
    public EnforcementReportRowDto map(DefendantAccountEntity entity, ReportMetadataContext context) {
        context.addParticipant(String.valueOf(entity.getDefendantAccountId()), PdplIdentifierType.DEFENDANT_ACCOUNT);
        setDelegate(delegate);
        setDebtorService(debtorService);
        setEnforcementService(enforcementService);
        EnforcementReportRowDto dto = delegate.map(entity, context);

        applyParty(entity, dto, context);
        applyLatestEnforcement(entity, dto);
        applyParentGuardian(entity, dto, context);
        applyFallbacks(entity, dto);

        return dto;
    }

    private void applyParty(DefendantAccountEntity entity, EnforcementReportRowDto dto, ReportMetadataContext context) {
        PartyEntity party = pickPrimaryParty(entity);
        if (party == null) {
            return;
        }
        delegate.mapParty(party, dto);
        Long partyId = party.getPartyId();
        if (partyId != null && debtorService != null) {
            debtorService.findByPartyId(partyId)
                .ifPresent(debtor -> {
                    context.addParticipant(String.valueOf(debtor.getPartyId()), PdplIdentifierType.DEBTOR_ACCOUNT);
                    delegate.mapDebtor(debtor, dto);
                });
        }
    }

    private PartyEntity pickPrimaryParty(DefendantAccountEntity account) {
        if (account == null || account.getParties() == null) {
            return null;
        }

        PartyEntity debtorFallback = null;
        PartyEntity anyFallback = null;

        for (DefendantAccountPartiesEntity link : account.getParties()) {
            if (link == null) {
                continue;
            }

            if (AssociationType.DEFENDANT.equals(link.getAssociationType())) {
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

    private void applyLatestEnforcement(DefendantAccountEntity entity, EnforcementReportRowDto dto) {
        Long accountId = entity.getDefendantAccountId();
        if (accountId == null || enforcementService == null) {
            return;
        }

        enforcementService.getEnforcementMostRecent(accountId)
            .ifPresent(latest -> {
                LocalDateTime posted = latest.getPostedDate();
                if (posted != null) {
                    dto.setLastEnforcementDate(posted.toLocalDate());
                }

                dto.setEnforcementReason(latest.getReason());
                dto.setUser(latest.getPostedBy());
                dto.setWarrantRef(latest.getWarrantReference());

                dto.setEnforcingCourtCode(
                    latest.getHearingCourtId() == null
                        ? null
                        : String.valueOf(latest.getHearingCourtId())
                );

                String resultId = latest.getResultId() != null
                    ? latest.getResultId()
                    : entity.getLastEnforcement();

                if ("PRIS".equalsIgnoreCase(resultId)
                    && latest.getHearingDate() != null) {
                    dto.setEarliestReleaseDate(latest.getHearingDate().toLocalDate());
                }
            });
    }

    private void applyParentGuardian(DefendantAccountEntity entity, EnforcementReportRowDto dto,
        ReportMetadataContext context) {
        if (entity.getParties() == null) {
            dto.setParentOrGuardian(null);
            return;
        }
        List<Long> parentGuardianIds = entity.getParties().stream()
            .filter(Objects::nonNull)
            .filter(p -> AssociationType.PARENT_GUARDIAN.equals(p.getAssociationType()))
            .map(DefendantAccountPartiesEntity::getDefendantAccountPartyId)
            .toList();

        for (Long parentGuardianId : parentGuardianIds) {
            context.addParticipant(String.valueOf(parentGuardianId), PdplIdentifierType.PARENT_GUARDIAN);
        }
        dto.setParentOrGuardian(parentGuardianIds.isEmpty() ? NO : YES);
    }

    private void applyFallbacks(DefendantAccountEntity entity, EnforcementReportRowDto dto) {
        if (dto.getParentOrGuardian() == null) {
            dto.setParentOrGuardian(entity.getProsecutorCaseReference());
        }

        if (dto.getJailDays() == null) {
            dto.setJailDays(entity.getJailDays());
        }
    }

    @Override
    public void mapParty(PartyEntity party, EnforcementReportRowDto dto) {
        delegate.mapParty(party, dto);
    }

    @Override
    public void mapDebtor(DebtorDetailEntity debtor, EnforcementReportRowDto dto) {
        delegate.mapDebtor(debtor, dto);
    }
}