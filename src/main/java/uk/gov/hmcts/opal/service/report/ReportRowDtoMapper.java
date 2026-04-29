package uk.gov.hmcts.opal.service.report;

import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
@DecoratedWith(ReportRowDtoCoreMapperDecorator.class)
public interface ReportRowDtoMapper extends ReportRowDtoCoreMapper {

}
