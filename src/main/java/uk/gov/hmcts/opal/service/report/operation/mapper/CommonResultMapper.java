package uk.gov.hmcts.opal.service.report.operation.mapper;

import java.util.List;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.service.report.ReportDataInterface;

public interface CommonResultMapper {

    ReportDataInterface map(List<DefendantAccountEntity> accounts);

}
