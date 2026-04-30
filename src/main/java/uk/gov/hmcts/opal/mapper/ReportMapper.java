package uk.gov.hmcts.opal.mapper;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.entity.ReportEntity;
import uk.gov.hmcts.opal.generated.model.ReportReports;
import uk.gov.hmcts.opal.mapper.helper.DurationMapperHelper;
import uk.gov.hmcts.opal.mapper.helper.JsonMapperHelper;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {JsonMapperHelper.class, DurationMapperHelper.class}
)
public abstract class ReportMapper {

    @Mapping(target = "supportedFileTypes", source = "supportedFileTypes", qualifiedByName = "toFileTypeEnums")
    @Mapping(target = "reportParameters", source = "reportParameters", qualifiedByName = "parseJsonToMap")
    @Mapping(target = "supportsMultipleBusinessUnits", source = "supportsMultiBu")
    @Mapping(target = "retentionPeriod", source = "retentionPeriod", qualifiedByName = "durationToString")
    public abstract ReportReports toDto(ReportEntity entity);

    @Named("toFileTypeEnums")
    protected List<ReportReports.SupportedFileTypesEnum> toFileTypeEnums(List<String> fileTypes) {
        if (fileTypes == null || fileTypes.isEmpty()) {
            return Collections.emptyList();
        }
        return fileTypes.stream()
            .map(type -> {
                try {
                    return ReportReports.SupportedFileTypesEnum.fromValue(type);
                } catch (IllegalArgumentException e) {
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .toList();
    }
}
