package uk.gov.hmcts.opal.service.report.operation.mapper;

import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
@DecoratedWith(SummaryRowDtoMapperDecorator.class)
public interface SummaryRowDtoMapper
    extends SummaryRowDtoCoreMapper {

}
