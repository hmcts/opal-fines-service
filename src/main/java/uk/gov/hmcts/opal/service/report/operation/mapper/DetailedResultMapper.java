package uk.gov.hmcts.opal.service.report.operation.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.opal.dto.report.operation.DetailedAccountReportDto;
import uk.gov.hmcts.opal.dto.report.operation.DetailedOperationReportAccountRowDto;
import uk.gov.hmcts.opal.dto.report.operation.DetailedReportDto;
import uk.gov.hmcts.opal.dto.report.operation.DetailedReportTransactionRowDto;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.repository.DefendantTransactionRepository;
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

    @Autowired
    public void setRowMapper(DetailedRowDtoCoreMapper rowMapper,
        DetailedTransactionRowMapper transactionRowMapper,
        DefendantTransactionRepository transactionRepository) {
        this.rowMapper = rowMapper;
        this.transactionRowMapper = transactionRowMapper;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public OperationDetailedReport map(
        List<DefendantAccountEntity> accounts) {
        ReportMetadataContext context = new ReportMetadataContext();
        List<DetailedAccountReportDto> accountTransactionReports = accounts.stream()
            .map(account -> {
                DetailedOperationReportAccountRowDto accountRow =
                    rowMapper.map(account, context);

                List<DetailedReportTransactionRowDto> transactionRows =
                    transactionRepository.findByDefendantAccountId(account.getDefendantAccountId())
                        .stream()
                        .map(transaction -> transactionRowMapper.map(transaction, account, context))
                        .toList();

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