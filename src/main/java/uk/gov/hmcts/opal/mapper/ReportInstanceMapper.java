package uk.gov.hmcts.opal.mapper;

import static uk.gov.hmcts.opal.entity.report.ReportInstanceGenerationStatus.READY;

import java.util.List;
import java.util.Optional;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.entity.report.ReportInstanceGenerationStatus;
import uk.gov.hmcts.opal.entity.report.SupportedFileType;
import uk.gov.hmcts.opal.generated.model.BusinessUnitSummaryCommon;
import uk.gov.hmcts.opal.generated.model.CreateReportInstanceResponseReports;
import uk.gov.hmcts.opal.generated.model.ReportInstanceListReportsInner;
import uk.gov.hmcts.opal.generated.model.StatusReports;
import uk.gov.hmcts.opal.generated.model.UserByNameDetailsCommon;

@Mapper(componentModel = "spring")
public interface ReportInstanceMapper {

    CreateReportInstanceResponseReports toResponseDto(ReportInstanceEntity entity);

    @Mapping(target = "instanceId", source = "reportInstanceId")
    @Mapping(target = "reportId", source = "report.reportId")
    @Mapping(target = "requestedAt", source = "requestedAt")
    @Mapping(target = "generatedAt", source = "createdTimestamp")
    @Mapping(target = "requestedBy", source = ".")
    @Mapping(target = "name", expression = "java(mapReportName(instance))")
    @Mapping(target = "businessUnits", expression = "java(mapBusinessUnits(instance.getBusinessUnit()))")
    @Mapping(target = "status", source = "generationStatus")
    @Mapping(target = "numberOfRecords", source = "noOfRecords")
    @Mapping(target = "isDownloadable", expression = "java(calculateIsDownloadable(instance))")
    @Mapping(target = "supportedFileTypes", source = "report.supportedFileTypes")
    ReportInstanceListReportsInner toReportInstanceListReportsInnerDto(ReportInstanceEntity instance);

    default String mapReportName(ReportInstanceEntity instance) {
        return Optional.ofNullable(instance.getReportName())
            .filter(name -> !name.isBlank())
            .orElseGet(() -> instance.getReport().getReportTitle());
    }

    @Mapping(target = "code", source = "status")
    @Mapping(target = "displayName", source = "status.displayName")
    StatusReports mapStatus(ReportInstanceGenerationStatus status);

    default boolean calculateIsDownloadable(ReportInstanceEntity instance) {
        return instance.getGenerationStatus() == READY
            && instance.getReport().getSupportedFileTypes() != null
            && !instance.getReport().getSupportedFileTypes().isEmpty();
    }

    @Mapping(target = "userId", source = "requestedBy")
    @Mapping(target = "name", source = "requestedByName")
    UserByNameDetailsCommon mapRequestedBy(ReportInstanceEntity instance);

    default List<BusinessUnitSummaryCommon> mapBusinessUnits(List<Integer> businessUnitIds) {
        if (businessUnitIds == null) {
            return List.of();
        }
        return businessUnitIds.stream()
            .map(id -> new BusinessUnitSummaryCommon().businessUnitId(String.valueOf(id)))
            .toList();
    }

    default List<ReportInstanceListReportsInner.SupportedFileTypesEnum> mapSupportedFileTypes(
        List<SupportedFileType> types) {
        if (types == null) {
            return List.of();
        }
        return types.stream()
            .map(type -> ReportInstanceListReportsInner.SupportedFileTypesEnum.fromValue(type.name()))
            .toList();
    }
}
