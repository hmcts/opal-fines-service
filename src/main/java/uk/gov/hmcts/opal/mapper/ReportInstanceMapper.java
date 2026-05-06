package uk.gov.hmcts.opal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.hmcts.opal.dto.response.ReportInstanceResponse;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.generated.model.CreateReportInstanceResponseReports;

@Mapper(componentModel = "spring")
public interface ReportInstanceMapper {


    //@Mapping(source = "report.reportId", target = "reportId")
    CreateReportInstanceResponseReports toResponseDto(ReportInstanceEntity entity);
}
