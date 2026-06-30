package uk.gov.hmcts.opal.service.report.operationbyenforcement.mapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.opal.dto.report.operationbyenforcement.OperationByEnforcementDetailedAccountReportDto;
import uk.gov.hmcts.opal.dto.report.operationbyenforcement.OperationByEnforcementDetailedReportAccountRowDto;
import uk.gov.hmcts.opal.dto.report.operationbyenforcement.OperationByEnforcementDetailedReportDto;
import uk.gov.hmcts.opal.dto.report.operationbyenforcement.OperationByEnforcementDetailedReportTransactionRowDto;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.defendanttransaction.DefendantTransactionEntity;
import uk.gov.hmcts.opal.entity.imposition.ImpositionEntity;
import uk.gov.hmcts.opal.repository.DefendantTransactionRepository;
import uk.gov.hmcts.opal.repository.ImpositionRepository;
import uk.gov.hmcts.opal.service.report.operationbyenforcement.OperationByEnforcementDetailedReport;
import uk.gov.hmcts.opal.service.report.ReportMetaData;
import uk.gov.hmcts.opal.service.report.ReportMetadataContext;


@Mapper(componentModel = "spring", uses = {
    DetailedRowDtoCoreMapper.class
}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class DetailedResultMapper
    implements CommonResultMapper {

    protected DetailedRowDtoCoreMapper rowMapper;
    protected DetailedTransactionRowMapper transactionRowMapper;
    protected DefendantTransactionRepository transactionRepository;
    protected ImpositionRepository impositionRepository;

    @Autowired
    public void setRowMapper(DetailedRowDtoCoreMapper rowMapper,
        DetailedTransactionRowMapper transactionRowMapper,
        DefendantTransactionRepository transactionRepository,
        ImpositionRepository impositionRepository) {
        this.rowMapper = rowMapper;
        this.transactionRowMapper = transactionRowMapper;
        this.transactionRepository = transactionRepository;
        this.impositionRepository = impositionRepository;
    }

    @Override
    public OperationByEnforcementDetailedReport map(List<DefendantAccountEntity> accounts) {
        //todo mapping starts here
        ReportMetadataContext context = new ReportMetadataContext();
        List<OperationByEnforcementDetailedAccountReportDto> accountTransactionReports = accounts.stream()
            .map(account -> {
                OperationByEnforcementDetailedReportAccountRowDto accountRow =
                    rowMapper.map(account, context);
                //TODO include additional history here: enforcements, notes, paymentTerms and transactions (curr)
                //todo see DefendantAccountHistoryService.getHistory() && PO-8659
                List<DefendantTransactionEntity> transactionEntities = transactionRepository
                    .findByDefendantAccountId(account.getDefendantAccountId());
                List<Long> impositionIds = transactionEntities.stream()
                    .filter(transaction ->
                        AssociatedRecordType.IMPOSITIONS.equals(transaction.getAssociatedRecordType()))
                    .map(transaction ->
                        Long.valueOf(transaction.getAssociatedRecordId()))
                    .toList();
                Map<String, ImpositionEntity> impositionsForTransactions = impositionRepository.findAllById(impositionIds)
                    .stream().collect(Collectors.toMap(
                        imposition -> imposition.getImpositionId().toString(),
                        imposition -> imposition));
                List<OperationByEnforcementDetailedReportTransactionRowDto> transactionRows = transactionEntities
                        .stream()
                        .map(transaction -> transactionRowMapper.map(transaction, account,
                            impositionsForTransactions.get(transaction.getAssociatedRecordId()), context))
                        .toList();

                return OperationByEnforcementDetailedAccountReportDto.builder()
                    .accountRow(accountRow)
                    .transactionRows(transactionRows)
                    .build();
            })
            .toList();

        OperationByEnforcementDetailedReportDto reportDto = OperationByEnforcementDetailedReportDto.builder()
            .accountTransactionReports(accountTransactionReports)
            .build();

        OperationByEnforcementDetailedReport report = new OperationByEnforcementDetailedReport();
        report.setEnforcementReport(reportDto);
        ReportMetaData meta = new ReportMetaData();
        meta.setPdpoPartyIds(context.getParticipants());
        report.setReportMetaData(meta);
        return report;
    }
}