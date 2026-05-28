package uk.gov.hmcts.opal.mapper;

import org.mapstruct.Mapper;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.generated.model.CreateReportInstanceResponseReports;

@Mapper(componentModel = "spring")
public interface ReportInstanceMapper {

    CreateReportInstanceResponseReports toResponseDto(ReportInstanceEntity entity);
}
