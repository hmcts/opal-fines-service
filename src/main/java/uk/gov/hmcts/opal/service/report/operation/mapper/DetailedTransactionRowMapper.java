package uk.gov.hmcts.opal.service.report.operation.mapper;

import static uk.gov.hmcts.opal.dto.PdplIdentifierType.CONSOLIDATED_ACCOUNT;
import static uk.gov.hmcts.opal.entity.paymentterms.TermsTypeCode.BY_DATE;
import static uk.gov.hmcts.opal.service.report.CommonReportStringConstants.SPACED_PIPE;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.opal.dto.report.operation.DetailedReportTransactionRowDto;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.entity.NoteEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.defendanttransaction.DefendantTransactionEntity;
import uk.gov.hmcts.opal.entity.defendanttransaction.DefendantTransactionType;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity;
import uk.gov.hmcts.opal.entity.imposition.ImpositionEntity;
import uk.gov.hmcts.opal.entity.paymentterms.PaymentTermsEntity;
import uk.gov.hmcts.opal.service.opal.history.defendant.DefendantTransactionDetailsService;
import uk.gov.hmcts.opal.service.persistence.DefendantAccountRepositoryService;
import uk.gov.hmcts.opal.service.report.ReportMetadataContext;

@Mapper(componentModel = "spring")
public abstract class DetailedTransactionRowMapper
    implements CommonMappingHelper {
    public static final String PAYMENT_TERMS_TYPE = "TTPAY";
    public static final String ENFORCEMENT_TYPE = "ENFT";
    public static final String NOTE_TYPE = "NOTE";

    @Autowired
    protected DefendantAccountRepositoryService defendantAccountService;

    @Autowired
    protected DefendantTransactionDetailsService defendantTransactionDetailsService;

    @Mapping(target = "accountNo", source = "account.accountNumber")
    @Mapping(target = "transactionDate", source = "transaction.postedDate")
    @Mapping(target = "transactionType", source = "transaction.transactionType")
    @Mapping(target = "transactionUserId", source = "transaction.postedByUsername")
    @Mapping(target = "transactionAmount", source = "transaction.transactionAmount")
    @Mapping(target = "consolidatedAccountNo", expression = "java(getConsolidatedAccountNo(transaction, context))")
    @Mapping(target = "transactionDetails",
        expression = "java(getTransactionDetails(transaction, account, imposition))")
    public abstract DetailedReportTransactionRowDto mapFromTransaction(
        DefendantTransactionEntity transaction,
        DefendantAccountEntity account,
        ImpositionEntity imposition,
        ReportMetadataContext context
    );

    @Mapping(target = "accountNo", source = "account.accountNumber")
    @Mapping(target = "transactionDate", source = "paymentTerms.postedDate")
    @Mapping(target = "transactionType", constant = PAYMENT_TERMS_TYPE)
    @Mapping(target = "transactionUserId", source = "paymentTerms.postedByUsername")
    @Mapping(target = "transactionAmount", ignore = true)
    @Mapping(target = "consolidatedAccountNo", ignore = true)
    @Mapping(target = "transactionDetails", expression = "java(getPaymentTermsDetails(paymentTerms))")
    public abstract DetailedReportTransactionRowDto mapFromPaymentTerms(PaymentTermsEntity paymentTerms,
        DefendantAccountEntity account,
        ReportMetadataContext context);

    @Mapping(target = "accountNo", source = "account.accountNumber")
    @Mapping(target = "transactionDate", source = "enforcement.postedDate")
    @Mapping(target = "transactionType", constant = ENFORCEMENT_TYPE)
    @Mapping(target = "transactionUserId", source = "enforcement.postedByUsername")
    @Mapping(target = "transactionAmount", ignore = true)
    @Mapping(target = "consolidatedAccountNo", ignore = true)
    @Mapping(target = "transactionDetails", expression = "java(getEnforcementDetails(enforcement))")
    public abstract DetailedReportTransactionRowDto mapFromEnforcement(EnforcementEntity enforcement,
        DefendantAccountEntity account,
        ReportMetadataContext context);

    @Mapping(target = "accountNo", source = "account.accountNumber")
    @Mapping(target = "transactionDate", source = "note.postedDate")
    @Mapping(target = "transactionType", constant = NOTE_TYPE)
    @Mapping(target = "transactionUserId", source = "note.postedByUsername")
    @Mapping(target = "transactionAmount", ignore = true)
    @Mapping(target = "consolidatedAccountNo", expression = "java(getConsolidatedAccountNo("
        + "note.getAssociatedRecordType(), note.getAssociatedRecordId(), context))")
    @Mapping(target = "transactionDetails", source = "note.noteText")
    public abstract DetailedReportTransactionRowDto mapFromNote(NoteEntity note, DefendantAccountEntity account,
        ReportMetadataContext context);


    protected String getTransactionDetails(DefendantTransactionEntity transaction,
        DefendantAccountEntity account, ImpositionEntity imposition) {
        return defendantTransactionDetailsService.generateTransactionDetails(transaction, account, imposition);
    }

    protected String getPaymentTermsDetails(PaymentTermsEntity paymentTerms) {
        StringBuilder sb = new StringBuilder();
        if (paymentTerms.getTermsTypeCode() == BY_DATE) {
            sb.append("In full | ");
        } else {
            if (paymentTerms.getInstalmentLumpSum() != null) {
                sb.append("Lump sum: ").append(paymentTerms.getInstalmentLumpSum()).append(SPACED_PIPE);
            }
            if (paymentTerms.getInstalmentAmount() != null && paymentTerms.getInstalmentPeriod() != null) {
                sb.append("Installments: ").append(paymentTerms.getInstalmentAmount()).append(" ");
                sb.append(paymentTerms.getInstalmentPeriod().getReportText()).append(" from ");
            }
        }
        sb.append(paymentTerms.getEffectiveDate());
        if (paymentTerms.getJailDays() != null) {
            sb.append(SPACED_PIPE).append(paymentTerms.getJailDays()).append(" days in default");
        }
        if (paymentTerms.getReasonForExtension() != null) {
            sb.append(SPACED_PIPE).append(paymentTerms.getReasonForExtension());
        }
        return sb.toString();
    }

    protected String getEnforcementDetails(EnforcementEntity enforcement) {
        StringBuilder sb = new StringBuilder();
        sb.append(enforcement.getResultId());
        if (enforcement.getJailDays() != null) {
            sb.append(SPACED_PIPE).append(enforcement.getJailDays()).append(" days in default");
        }
        if (enforcement.getWarrantReference() != null) {
            sb.append(SPACED_PIPE).append("Warrant number: ").append(enforcement.getWarrantReference());
        }
        if (enforcement.getEarliestReleaseDate() != null) {
            sb.append(SPACED_PIPE).append("Earliest date of release: ").append(enforcement.getEarliestReleaseDate());
        }
        if (enforcement.getHearingDate() != null) {
            sb.append(SPACED_PIPE).append("Hearing: ").append(enforcement.getHearingDate())
                .append(" - ").append(enforcement.getHearingCourt().getName())
                .append(" - Case: ").append(enforcement.getCaseReference());
        }
        if (enforcement.getReason() != null) {
            sb.append(SPACED_PIPE).append(enforcement.getReason());
        }
        return sb.toString();
    }

    protected String getConsolidatedAccountNo(DefendantTransactionEntity entity, ReportMetadataContext context) {
        if (entity.getTransactionType() == DefendantTransactionType.CONSOL) {
            return getConsolidatedAccountNo(entity.getAssociatedRecordType(), entity.getAssociatedRecordId(), context);
        }
        return null;
    }

    protected String getConsolidatedAccountNo(AssociatedRecordType type, String id, ReportMetadataContext context) {
        if (type == AssociatedRecordType.DEFENDANT_ACCOUNTS) {
            long accountId = Long.parseLong(id);
            DefendantAccountEntity account = defendantAccountService.findById(accountId);
            context.addParticipant(id, CONSOLIDATED_ACCOUNT);
            return account.getAccountNumber();
        }
        return null;
    }
}