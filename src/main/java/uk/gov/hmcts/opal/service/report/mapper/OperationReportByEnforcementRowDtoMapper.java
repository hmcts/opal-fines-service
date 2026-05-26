package uk.gov.hmcts.opal.service.report.mapper;

import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
@DecoratedWith(OperationReportByEnforcementRowDtoCoreMapperDecorator.class)
public interface OperationReportByEnforcementRowDtoMapper extends OperationReportByEnforcementRowDtoCoreMapper {

}
