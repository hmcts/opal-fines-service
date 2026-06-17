package uk.gov.hmcts.opal.service.report.operationbyenforcement.mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.opal.dto.report.operationbyenforcement.OperationByEnforcementSummaryReportDto;
import uk.gov.hmcts.opal.dto.report.operationbyenforcement.OperationByEnforcementSummaryReportRowDto;
import uk.gov.hmcts.opal.dto.report.operationbyenforcement.OperationByEnforcementSummaryReportTotalsRowDto;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.service.report.operationbyenforcement.OperationByEnforcementSummaryReport;
import uk.gov.hmcts.opal.service.report.ReportMetaData;
import uk.gov.hmcts.opal.service.report.ReportMetadataContext;


@Mapper(componentModel = "spring", uses = {
    SummaryRowDtoCoreMapper.class
}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class SummaryResultMapper
    implements CommonResultMapper {

    protected SummaryRowDtoCoreMapper rowMapper;

    @Autowired
    public void setRowMapper(SummaryRowDtoCoreMapper rowMapper) {
        this.rowMapper = rowMapper;
    }

    public OperationByEnforcementSummaryReport map(
        List<DefendantAccountEntity> accounts) {
        ReportMetadataContext context = new ReportMetadataContext();
        List<OperationByEnforcementSummaryReportRowDto> rows = accounts.stream()
            .map(acc -> rowMapper.map(acc, context))
            .toList();

        BigDecimal totalBalance = rows.stream()
            .map(OperationByEnforcementSummaryReportRowDto::getBalance)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPaid = rows.stream()
            .map(OperationByEnforcementSummaryReportRowDto::getAmountPaid)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalImposed = rows.stream()
            .map(OperationByEnforcementSummaryReportRowDto::getAmountImposed)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        OperationByEnforcementSummaryReportTotalsRowDto totals =
            OperationByEnforcementSummaryReportTotalsRowDto.builder()
                .accountsReported(rows.size())
                .totalPaid(totalPaid)
                .totalImposed(totalImposed)
                .totalBalance(totalBalance)
                .build();

        OperationByEnforcementSummaryReport report = new OperationByEnforcementSummaryReport();
        report.setEnforcementReport(OperationByEnforcementSummaryReportDto.builder()
            .reportSummaryRows(rows)
            .totals(totals)
            .build());
        ReportMetaData meta = new ReportMetaData();
        meta.setPdpoPartyIds(context.getParticipants());
        report.setReportMetaData(meta);
        return report;
    }
}
