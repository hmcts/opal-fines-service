package uk.gov.hmcts.opal.service.report.operation.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.report.operation.DetailedAccountReportDto;
import uk.gov.hmcts.opal.dto.report.operation.DetailedOperationReportAccountRowDto;
import uk.gov.hmcts.opal.dto.report.operation.DetailedReportDto;
import uk.gov.hmcts.opal.dto.report.operation.DetailedReportTransactionRowDto;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.entity.NoteEntity;
import uk.gov.hmcts.opal.entity.NoteType;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.defendanttransaction.DefendantTransactionEntity;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity;
import uk.gov.hmcts.opal.entity.imposition.ImpositionEntity;
import uk.gov.hmcts.opal.entity.paymentterms.PaymentTermsEntity;
import uk.gov.hmcts.opal.repository.DefendantTransactionRepository;
import uk.gov.hmcts.opal.repository.EnforcementRepository;
import uk.gov.hmcts.opal.repository.ImpositionRepository;
import uk.gov.hmcts.opal.repository.NoteRepository;
import uk.gov.hmcts.opal.repository.PaymentTermsRepository;
import uk.gov.hmcts.opal.service.report.ReportMetaData;
import uk.gov.hmcts.opal.service.report.ReportMetadataContext;
import uk.gov.hmcts.opal.service.report.operation.OperationDetailedReport;


@Mapper(componentModel = "spring", uses = {
    DetailedRowDtoCoreMapper.class
}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class DetailedResultMapper
    implements CommonResultMapper {

    protected DetailedRowDtoCoreMapper rowMapper;
    protected DetailedTransactionRowMapper transactionRowMapper;
    protected DefendantTransactionRepository transactionRepository;
    protected ImpositionRepository impositionRepository;
    protected PaymentTermsRepository paymentTermsRepository;
    protected EnforcementRepository enforcementRepository;
    protected NoteRepository noteRepository;

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
    public OperationDetailedReport map(List<DefendantAccountEntity> accounts) {
        ReportMetadataContext context = new ReportMetadataContext();
        List<DetailedAccountReportDto> accountTransactionReports = accounts.stream()
            .map(account -> {
                DetailedOperationReportAccountRowDto accountRow =
                    rowMapper.map(account, context);
                //TTPAY
                List<PaymentTermsEntity> paymentTermsEntities = paymentTermsRepository
                    .findByDefendantAccount_DefendantAccountIdAndEffectiveDateIsNotNullOrderByEffectiveDateAsc(
                        account.getDefendantAccountId());
                //ENFT
                List<EnforcementEntity> enforcementEntities = enforcementRepository
                    .findHistoryRowsByDefendantAccountId(account.getDefendantAccountId());
                //(transaction)
                List<DefendantTransactionEntity> transactionEntities = transactionRepository
                    .findByDefendantAccountId(account.getDefendantAccountId());
                List<Long> impositionIds = transactionEntities.stream()
                    .filter(transaction ->
                        AssociatedRecordType.IMPOSITIONS.equals(transaction.getAssociatedRecordType()))
                    .map(transaction ->
                        Long.valueOf(transaction.getAssociatedRecordId()))
                    .toList();
                Map<String, ImpositionEntity> impositionsForTransactions = impositionRepository
                    .findAllById(impositionIds)
                    .stream().collect(Collectors.toMap(
                        imposition -> imposition.getImpositionId().toString(),
                        imposition -> imposition));
                //NOTE
                List<NoteEntity> noteEntities = noteRepository.findAll(Specification.allOf(
                    (root, query, builder) ->
                        builder.and(
                            builder.equal(
                                root.get("associatedRecordType").as(String.class),
                                AssociatedRecordType.DEFENDANT_ACCOUNTS.getLabel()
                            ),
                            builder.equal(root.get("associatedRecordId"), account.getDefendantAccountId().toString()),
                            builder.equal(root.get("noteType").as(String.class), NoteType.AA.name())
                        )
                ));

                //TTPAY
                List<DetailedReportTransactionRowDto> transactionRows = new ArrayList<>(paymentTermsEntities.stream()
                    .map(paymentTerms -> transactionRowMapper.mapFromPaymentTerms(
                        paymentTerms, account, context))
                    .toList());
                //ENFT
                transactionRows.addAll(enforcementEntities.stream().map(enforcement -> transactionRowMapper
                        .mapFromEnforcement(enforcement, account, context))
                    .toList());
                //transactions
                transactionRows.addAll(transactionEntities.stream().map(transaction -> transactionRowMapper
                        .mapFromTransaction(transaction, account, impositionsForTransactions.get(transaction.getAssociatedRecordId()),
                            context))
                    .toList());
                //NOTE
                transactionRows.addAll(noteEntities.stream().map(note -> transactionRowMapper
                        .mapFromNote(note, account, context))
                    .toList());
                Collections.sort(transactionRows);
                return DetailedAccountReportDto.builder()
                    .accountRow(accountRow)
                    .transactionRows(transactionRows)
                    .build();
            })
            .toList();

        DetailedReportDto reportDto = DetailedReportDto.builder()
            .accountTransactionReports(accountTransactionReports)
            .build();

        OperationDetailedReport report = new OperationDetailedReport();
        report.setDetailedReport(reportDto);
        ReportMetaData meta = new ReportMetaData();
        meta.setPdpoPartyIds(context.getParticipants());
        report.setReportMetaData(meta);
        return report;
    }
}