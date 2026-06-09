package uk.gov.hmcts.opal.service.report.operationbyenforcement.mapper;

import static uk.gov.hmcts.opal.dto.PdplIdentifierType.CONSOLIDATED_ACCOUNT;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.opal.dto.report.operationbyenforcement.OperationByEnforcementDetailedReportTransactionRowDto;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.defendanttransaction.DefendantTransactionEntity;
import uk.gov.hmcts.opal.entity.defendanttransaction.DefendantTransactionType;
import uk.gov.hmcts.opal.service.persistence.DefendantAccountRepositoryService;
import uk.gov.hmcts.opal.service.report.ReportMetadataContext;

@Mapper(componentModel = "spring")
public abstract class DetailedTransactionRowMapper
    implements CommonMappingHelper {

    @Autowired
    protected DefendantAccountRepositoryService defendantAccountService;

    @Mapping(target = "accountNo", source = "account.accountNumber")
    @Mapping(target = "transactionDate", source = "transaction.postedDate")
    @Mapping(target = "transactionType", source = "transaction.transactionType")
    @Mapping(target = "transactionUserId", source = "transaction.postedByUsername")
    @Mapping(target = "transactionAmount", source = "transaction.transactionAmount")
    @Mapping(target = "consolidatedAccountNo", expression = "java(getConsolidatedAccountNo(transaction, context))")
    public abstract OperationByEnforcementDetailedReportTransactionRowDto map(
        DefendantTransactionEntity transaction,
        DefendantAccountEntity account,
        ReportMetadataContext context
    );

    protected String getConsolidatedAccountNo(DefendantTransactionEntity entity, ReportMetadataContext context) {
        if (DefendantTransactionType.CONSOL.equals(entity.getTransactionType())
            && AssociatedRecordType.DEFENDANT_ACCOUNTS.equals(entity.getAssociatedRecordType())) {
            long accountId = Long.parseLong(entity.getAssociatedRecordId());
            DefendantAccountEntity account = defendantAccountService.findById(accountId);
            context.addParticipant(entity.getAssociatedRecordId(), CONSOLIDATED_ACCOUNT);
            return account.getAccountNumber();
        }
        return null;
    }
}