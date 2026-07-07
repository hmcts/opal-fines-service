package uk.gov.hmcts.opal.service.report.operation.mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.opal.dto.report.operation.SummaryOperationReportRowDto;
import uk.gov.hmcts.opal.dto.report.operation.SummaryReportDto;
import uk.gov.hmcts.opal.dto.report.operation.SummaryReportTotalsRowDto;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.service.report.ReportMetadataContext;
import uk.gov.hmcts.opal.service.report.ReportMetaData;
import uk.gov.hmcts.opal.service.report.operation.OperationByEnforcementSummaryReport;
import uk.gov.hmcts.opal.service.report.operation.OperationByPaymentSummaryReport;


@Mapper(componentModel = "spring", uses = {
    SummaryRowDtoCoreMapper.class
}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class SummaryResultMapper {

    protected SummaryRowDtoCoreMapper rowMapper;

    @Autowired
    public void setRowMapper(SummaryRowDtoCoreMapper rowMapper) {
        this.rowMapper = rowMapper;
    }

    public OperationByEnforcementSummaryReport mapEnforcement(
        List<DefendantAccountEntity> accounts) {
        SummaryReportMappingResult mappingResult = mapSummaryReport(accounts);
        OperationByEnforcementSummaryReport report = new OperationByEnforcementSummaryReport();
        report.setEnforcementReport(mappingResult.reportDto());
        report.setReportMetaData(mappingResult.reportMetaData());
        return report;
    }

    public OperationByPaymentSummaryReport mapPayment(
        List<DefendantAccountEntity> accounts) {
        SummaryReportMappingResult mappingResult = mapSummaryReport(accounts);
        OperationByPaymentSummaryReport report = new OperationByPaymentSummaryReport();
        report.setPaymentReport(mappingResult.reportDto());
        report.setReportMetaData(mappingResult.reportMetaData());
        return report;
    }

    private SummaryReportMappingResult mapSummaryReport(
        List<DefendantAccountEntity> accounts) {
        ReportMetadataContext context = new ReportMetadataContext();
        List<SummaryOperationReportRowDto> rows = accounts.stream()
            .map(acc -> rowMapper.map(acc, context))
            .toList();

        BigDecimal totalBalance = rows.stream()
            .map(SummaryOperationReportRowDto::getBalance)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPaid = rows.stream()
            .map(SummaryOperationReportRowDto::getAmountPaid)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalImposed = rows.stream()
            .map(SummaryOperationReportRowDto::getAmountImposed)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        SummaryReportTotalsRowDto totals =
            SummaryReportTotalsRowDto.builder()
                .accountsReported(rows.size())
                .totalPaid(totalPaid)
                .totalImposed(totalImposed)
                .totalBalance(totalBalance)
                .build();

        SummaryReportDto reportDto = SummaryReportDto.builder()
            .reportSummaryRows(rows)
            .totals(totals)
            .build();
        ReportMetaData meta = new ReportMetaData();
        meta.setPdpoPartyIds(context.getParticipants());
        return new SummaryReportMappingResult(reportDto, meta);
    }

    private record SummaryReportMappingResult(
        SummaryReportDto reportDto,
        ReportMetaData reportMetaData
    ) {
    }
}
