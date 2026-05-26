package uk.gov.hmcts.opal.service.report.mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.dto.report.EnforcementReportDto;
import uk.gov.hmcts.opal.dto.report.EnforcementReportRowDto;
import uk.gov.hmcts.opal.dto.report.OperationReportByEnforcementTotalsRowDto;
import uk.gov.hmcts.opal.service.report.OperationReportByEnforcementTransaction;
import uk.gov.hmcts.opal.service.report.ReportMetaData;
import uk.gov.hmcts.opal.service.report.ReportMetadataContext;


@Mapper(componentModel = "spring", uses = {
    OperationReportByEnforcementRowDtoCoreMapper.class
}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class OperationReportByEnforcementResultMapper {

    protected OperationReportByEnforcementRowDtoCoreMapper rowMapper;

    @Autowired
    public void setRowMapper(OperationReportByEnforcementRowDtoCoreMapper rowMapper) {
        this.rowMapper = rowMapper;
    }

    public OperationReportByEnforcementTransaction map(
        List<DefendantAccountEntity> accounts) {
        ReportMetadataContext context = new ReportMetadataContext();
        List<EnforcementReportRowDto> rows = accounts.stream()
            .map(acc -> rowMapper.map(acc, context))
            .toList();

        BigDecimal totalBalance = rows.stream()
            .map(EnforcementReportRowDto::getBalance)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPaid = rows.stream()
            .map(EnforcementReportRowDto::getAmountPaid)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalImposed = rows.stream()
            .map(EnforcementReportRowDto::getAmountImposed)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        OperationReportByEnforcementTotalsRowDto totals = OperationReportByEnforcementTotalsRowDto.builder()
            .accountsReported(rows.size())
            .totalPaid(totalPaid)
            .totalImposed(totalImposed)
            .totalBalance(totalBalance)
            .build();

        OperationReportByEnforcementTransaction report = new OperationReportByEnforcementTransaction();
        report.setEnforcementReport(EnforcementReportDto.builder()
                .transactionList(rows)
                .totals(totals)
            .build());
        ReportMetaData meta = new ReportMetaData();
        meta.setPdpoPartyIds(context.getParticipants());
        report.setReportMetaData(meta);
        return report;
    }
}