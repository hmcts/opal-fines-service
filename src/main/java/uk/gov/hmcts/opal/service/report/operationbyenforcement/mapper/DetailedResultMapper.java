package uk.gov.hmcts.opal.service.report.operationbyenforcement.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.opal.dto.report.operationbyenforcement.OperationByEnforcementDetailedAccountReportDto;
import uk.gov.hmcts.opal.dto.report.operationbyenforcement.OperationByEnforcementDetailedReportAccountRowDto;
import uk.gov.hmcts.opal.dto.report.operationbyenforcement.OperationByEnforcementDetailedReportDto;
import uk.gov.hmcts.opal.dto.report.operationbyenforcement.OperationByEnforcementDetailedReportTransactionRowDto;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.repository.DefendantTransactionRepository;
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

    @Autowired
    public void setRowMapper(DetailedRowDtoCoreMapper rowMapper,
        DetailedTransactionRowMapper transactionRowMapper,
        DefendantTransactionRepository transactionRepository) {
        this.rowMapper = rowMapper;
        this.transactionRowMapper = transactionRowMapper;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public OperationByEnforcementDetailedReport map(
        List<DefendantAccountEntity> accounts) {
        ReportMetadataContext context = new ReportMetadataContext();
        List<OperationByEnforcementDetailedAccountReportDto> accountTransactionReports = accounts.stream()
            .map(account -> {
                OperationByEnforcementDetailedReportAccountRowDto accountRow =
                    rowMapper.map(account, context);

                List<OperationByEnforcementDetailedReportTransactionRowDto> transactionRows =
                    transactionRepository.findByDefendantAccountId(account.getDefendantAccountId())
                        .stream()
                        .map(transaction -> transactionRowMapper.map(transaction, account, context))
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