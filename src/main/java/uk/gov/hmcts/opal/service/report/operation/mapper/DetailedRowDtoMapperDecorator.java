package uk.gov.hmcts.opal.service.report.operation.mapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import uk.gov.hmcts.opal.dto.ImpositionTotalsDto;
import uk.gov.hmcts.opal.dto.PdplIdentifierType;
import uk.gov.hmcts.opal.dto.report.operation.DetailedOperationReportAccountRowDto;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.debtordetail.DebtorDetailEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.OriginatorType;
import uk.gov.hmcts.opal.service.DefendantAccountHeaderViewService;
import uk.gov.hmcts.opal.service.ImpositionService;
import uk.gov.hmcts.opal.service.persistence.DebtorDetailRepositoryService;
import uk.gov.hmcts.opal.service.persistence.PaymentTermsRepositoryService;
import uk.gov.hmcts.opal.service.report.ReportMetadataContext;

@Setter
public class DetailedRowDtoMapperDecorator
    implements DetailedRowDtoMapper {

    // Use @Autowired fields here because MapStruct-generated decorators
    // cannot use custom constructor injection.
    @Autowired
    @Qualifier("delegate")
    private DetailedRowDtoCoreMapper delegate;
    @Autowired
    private CommonRowMappingHelper helper;
    @Autowired
    private DebtorDetailRepositoryService debtorService;
    @Autowired
    private ImpositionService impositionService;
    @Autowired
    private PaymentTermsRepositoryService paymentTermsService;
    @Autowired
    private DefendantAccountHeaderViewService headerViewService;

    @Override
    public DetailedOperationReportAccountRowDto map(DefendantAccountEntity entity,
        ReportMetadataContext context) {
        context.addParticipant(String.valueOf(entity.getDefendantAccountId()), PdplIdentifierType.DEFENDANT_ACCOUNT);
        setDelegate(delegate);
        setDebtorService(debtorService);
        setImpositionService(impositionService);
        setHeaderViewService(headerViewService);
        DetailedOperationReportAccountRowDto dto = delegate.map(entity, context);

        applyAccountHeaderView(entity, dto);
        applyDateOfHearing(entity, dto);
        applyPaymentTerms(entity, dto);
        applyImpositions(entity, dto);
        helper.applyParty(entity, dto, context, delegate::mapParty, delegate::mapDebtor);
        String parentGuardian = helper.parentGuardianValue(entity);
        dto.setParentOrGuardian(parentGuardian);
        applyArrears(entity, dto);
        return dto;
    }

    private void applyAccountHeaderView(DefendantAccountEntity entity,
        DetailedOperationReportAccountRowDto dto) {
        BigDecimal arrears = headerViewService.getArrearsTotalForDefendantAccount(entity.getDefendantAccountId());
        dto.setArrearsTotal(arrears);
    }

    private void applyDateOfHearing(DefendantAccountEntity entity,
        DetailedOperationReportAccountRowDto dto) {
        OriginatorType originatorType = entity.getOriginatorType();
        LocalDate dateOfHearing = null;
        switch (originatorType) {
            case MAC_NEW_ACCOUNT -> dateOfHearing = entity.getImposedHearingDate();
            case FIXED_PENALTY ->
                dateOfHearing = impositionService.getEarliestImpositionDate(entity.getDefendantAccountId());
            case TRANSFER_IN_ACCOUNT -> {
                dateOfHearing = entity.getImposedHearingDate();
                if (dateOfHearing == null) {
                    dateOfHearing = impositionService.getEarliestImpositionDate(entity.getDefendantAccountId());
                }
            }
        }
        dto.setDateOfHearing(dateOfHearing);
    }

    private void applyImpositions(DefendantAccountEntity entity,
        DetailedOperationReportAccountRowDto dto) {
        Long accountId = entity.getDefendantAccountId();
        if (accountId == null || impositionService == null) {
            return;
        }
        ImpositionTotalsDto impositions = impositionService.getAccountImpositionTotals(accountId);
        delegate.mapImpositions(impositions, dto);
    }

    private void applyPaymentTerms(DefendantAccountEntity entity,
        DetailedOperationReportAccountRowDto dto) {
        Long accountId = entity.getDefendantAccountId();
        if (accountId == null || paymentTermsService == null) {
            return;
        }
        String paymentTerms = paymentTermsService.getPaymentTermsAsFormattedString(accountId);
        dto.setPaymentTerms(paymentTerms);
    }

    private void applyArrears(DefendantAccountEntity entity, DetailedOperationReportAccountRowDto dto) {
        Long accountId = entity.getDefendantAccountId();
        if (accountId == null || headerViewService == null) {
            return;
        }
        dto.setArrearsTotal(headerViewService.getArrearsTotalForDefendantAccount(accountId));
    }

    @Override
    public void mapParty(PartyEntity party, DetailedOperationReportAccountRowDto dto) {
        delegate.mapParty(party, dto);
    }

    @Override
    public void mapDebtor(DebtorDetailEntity debtor, DetailedOperationReportAccountRowDto dto) {
        delegate.mapDebtor(debtor, dto);
    }

    @Override
    public void mapImpositions(ImpositionTotalsDto impositions, DetailedOperationReportAccountRowDto dto) {
        delegate.mapImpositions(impositions, dto);
    }
}