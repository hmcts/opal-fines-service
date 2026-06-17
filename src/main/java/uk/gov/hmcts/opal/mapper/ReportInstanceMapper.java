package uk.gov.hmcts.opal.mapper;

import static uk.gov.hmcts.opal.entity.report.ReportInstanceGenerationStatus.READY;

import java.util.List;
import java.util.Optional;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.hmcts.opal.entity.ReportEntity;
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

    @Mapping(target = "instanceId", source = "instance.reportInstanceId")
    @Mapping(target = "reportId", source = "instance.report.reportId")
    @Mapping(target = "requestedAt", source = "instance.requestedAt")
    @Mapping(target = "generatedAt", source = "instance.createdTimestamp")
    @Mapping(target = "requestedBy", source = "instance")
    @Mapping(target = "name", expression = "java(mapReportName(instance, report))")
    @Mapping(target = "businessUnits", expression = "java(mapBusinessUnits(instance.getBusinessUnit()))")
    @Mapping(target = "status", source = "instance.generationStatus")
    @Mapping(target = "numberOfRecords", source = "instance.noOfRecords")
    @Mapping(target = "isDownloadable", expression = "java(calculateIsDownloadable(instance, report))")
    @Mapping(target = "supportedFileTypes", source = "report.supportedFileTypes")
    ReportInstanceListReportsInner toDto(ReportInstanceEntity instance, ReportEntity report);

    default String mapReportName(ReportInstanceEntity instance, ReportEntity report) {
        return Optional.ofNullable(instance.getReportName())
            .filter(name -> !name.isBlank())
            .orElseGet(report::getReportTitle);
    }

    @Mapping(target = "code", source = "status")
    @Mapping(target = "displayName", source = "status.displayName")
    StatusReports mapStatus(ReportInstanceGenerationStatus status);

    default boolean calculateIsDownloadable(ReportInstanceEntity instance, ReportEntity report) {
        return instance.getGenerationStatus() == READY
            && report.getSupportedFileTypes() != null
            && !report.getSupportedFileTypes().isEmpty();
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
