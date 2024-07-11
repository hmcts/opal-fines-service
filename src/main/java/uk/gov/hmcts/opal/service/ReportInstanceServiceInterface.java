package uk.gov.hmcts.opal.service;

import uk.gov.hmcts.opal.dto.search.ReportInstanceSearchDto;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;

import java.util.List;

public interface ReportInstanceServiceInterface {

    ReportInstanceEntity getReportInstance(long reportInstanceId);

    List<ReportInstanceEntity> searchReportInstances(ReportInstanceSearchDto criteria);
}
