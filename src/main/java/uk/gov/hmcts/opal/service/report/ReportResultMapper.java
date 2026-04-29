package uk.gov.hmcts.opal.service.report;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;


@Mapper(componentModel = "spring", uses = {
    ReportRowDtoCoreMapper.class
}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class ReportResultMapper {

    protected ReportRowDtoCoreMapper rowMapper;

    @Autowired
    public void setRowMapper(ReportRowDtoCoreMapper rowMapper) {
        this.rowMapper = rowMapper;
    }

    public OperationReportByEnforcementTransaction map(
        List<DefendantAccountEntity> accounts) {
        ReportMetadataContext context = new ReportMetadataContext();
        List<EnforcementReportRowDto> rows = accounts.stream()
            .map(acc -> rowMapper.map(acc, context))
            .toList();

        OperationReportByEnforcementTransaction report = new OperationReportByEnforcementTransaction();
        report.setTransactionList(rows);
        ReportMetaData meta = new ReportMetaData();
        meta.setPdpoPartyIds(context.getParticipants());
        report.setReportMetaData(meta);
        return report;
    }
}