package uk.gov.hmcts.opal.service.report.operationbyenforcement.mapper;

import java.time.LocalDateTime;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import uk.gov.hmcts.opal.dto.PdplIdentifierType;
import uk.gov.hmcts.opal.dto.report.operationbyenforcement.OperationByEnforcementSummaryReportRowDto;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.debtordetail.DebtorDetailEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.service.persistence.DebtorDetailRepositoryService;
import uk.gov.hmcts.opal.service.persistence.EnforcementRepositoryService;
import uk.gov.hmcts.opal.service.report.ReportMetadataContext;

@Setter
public class SummaryRowDtoMapperDecorator
    implements SummaryRowDtoMapper, CommonMappingHelper {

    // Use @Autowired fields here because MapStruct-generated decorators
    // cannot use custom constructor injection.
    @Autowired
    @Qualifier("delegate")
    private SummaryRowDtoCoreMapper delegate;
    @Autowired
    private CommonRowMappingHelper helper;
    @Autowired
    private DebtorDetailRepositoryService debtorService;
    @Autowired
    private EnforcementRepositoryService enforcementService;

    @Override
    public OperationByEnforcementSummaryReportRowDto map(DefendantAccountEntity entity, ReportMetadataContext context) {
        context.addParticipant(String.valueOf(entity.getDefendantAccountId()), PdplIdentifierType.DEFENDANT_ACCOUNT);
        setDelegate(delegate);
        setDebtorService(debtorService);
        setEnforcementService(enforcementService);
        OperationByEnforcementSummaryReportRowDto dto = delegate.map(entity, context);
        applyLatestEnforcement(entity, dto);
        helper.applyParty(entity, dto, context, delegate::mapParty, delegate::mapDebtor);
        String parentGuardian = helper.parentGuardianValue(entity);
        dto.setParentOrGuardian(parentGuardian);
        helper.applyParentGuardian(entity, context::addParticipant);
        return dto;
    }

    private void applyLatestEnforcement(DefendantAccountEntity entity, OperationByEnforcementSummaryReportRowDto dto) {
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

    @Override
    public void mapParty(PartyEntity party, OperationByEnforcementSummaryReportRowDto dto) {
        delegate.mapParty(party, dto);
    }

    @Override
    public void mapDebtor(DebtorDetailEntity debtor, OperationByEnforcementSummaryReportRowDto dto) {
        delegate.mapDebtor(debtor, dto);
    }
}