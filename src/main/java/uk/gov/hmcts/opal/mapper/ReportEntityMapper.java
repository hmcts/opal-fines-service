package uk.gov.hmcts.opal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.entity.ReportEntity;
import uk.gov.hmcts.opal.generated.model.ReportReports;
import uk.gov.hmcts.opal.mapper.helper.JsonMapperHelper;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {JsonMapperHelper.class}
)
public abstract class ReportEntityMapper {

    @Mapping(target = "reportParameters", source = "reportParameters", qualifiedByName = "parseJsonToMap")
    @Mapping(target = "supportsMultipleBusinessUnits", source = "supportsMultiBu")
    @Mapping(target = "retentionPeriod", source = "retentionPeriod")
    @Mapping(target = "isBespokeJourney", source = "bespokeJourney")
    public abstract ReportReports toDto(ReportEntity entity);
}
