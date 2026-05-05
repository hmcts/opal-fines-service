package uk.gov.hmcts.opal.mapper;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.entity.ReportEntity;
import uk.gov.hmcts.opal.entity.report.SupportedFileType;
import uk.gov.hmcts.opal.generated.model.ReportReports;
import uk.gov.hmcts.opal.mapper.helper.JsonMapperHelper;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {JsonMapperHelper.class}
)
public abstract class ReportMapper {

    @Mapping(target = "supportedFileTypes", source = "supportedFileTypes", qualifiedByName = "toFileTypeEnums")
    @Mapping(target = "reportParameters", source = "reportParameters", qualifiedByName = "parseJsonToMap")
    @Mapping(target = "supportsMultipleBusinessUnits", source = "supportsMultiBu")
    @Mapping(target = "retentionPeriod", source = "retentionPeriod", qualifiedByName = "durationToString")
    @Mapping(target = "isBespokeJourney", source = "bespokeJourney")
    public abstract ReportReports toDto(ReportEntity entity);

    @Named("toFileTypeEnums")
    protected List<ReportReports.SupportedFileTypesEnum> toFileTypeEnums(List<SupportedFileType> fileTypes) {
        if (fileTypes == null || fileTypes.isEmpty()) {
            return Collections.emptyList();
        }
        return fileTypes.stream()
            .map(type -> {
                try {
                    return ReportReports.SupportedFileTypesEnum.fromValue(type.name());
                } catch (IllegalArgumentException e) {
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .toList();
    }

    @Named("durationToString")
    protected String durationToString(Duration duration) {
        if (duration == null) {
            return null;
        }
        long days = duration.toDays();
        if (duration.equals(Duration.ofDays(days))) {
            return "P" + days + "D";
        }
        return duration.toString();
    }
}
