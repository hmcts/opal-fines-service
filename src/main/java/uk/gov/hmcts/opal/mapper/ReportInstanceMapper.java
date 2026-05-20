package uk.gov.hmcts.opal.mapper;

import org.mapstruct.Mapper;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.generated.model.CreateReportInstanceResponseReports;

@Mapper(componentModel = "spring")
public interface ReportInstanceMapper {


    //@Mapping(source = "report.reportId", target = "reportId")
    CreateReportInstanceResponseReports toResponseDto(ReportInstanceEntity entity);
}
