package uk.gov.hmcts.opal.mapper;

import static uk.gov.hmcts.opal.entity.report.ReportInstanceGenerationStatus.READY;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import org.mapstruct.AfterMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import uk.gov.hmcts.opal.entity.ReportEntity;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.entity.report.ReportInstanceGenerationStatus;
import uk.gov.hmcts.opal.entity.report.SupportedFileType;
import uk.gov.hmcts.opal.generated.model.BusinessUnitSummaryCommon;
import uk.gov.hmcts.opal.generated.model.CreateReportInstanceResponseReports;
import uk.gov.hmcts.opal.generated.model.ReportInstanceListReportsInner;
import uk.gov.hmcts.opal.generated.model.StatusReports;
import uk.gov.hmcts.opal.generated.model.UserByNameDetailsCommon;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.generated.model.ReportInstanceReports;
import uk.gov.hmcts.opal.generated.model.ReportReferenceReports;
import uk.gov.hmcts.opal.mapper.common.BusinessUnitSummaryMapper;
import uk.gov.hmcts.opal.mapper.helper.JsonMapperHelper;

@Mapper(componentModel = "spring",
    uses = {BusinessUnitSummaryMapper.class, JsonMapperHelper.class},
    builder = @Builder(disableBuilder = true))
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

    @Mapping(target = "instanceId", source = "reportInstanceEntity.reportInstanceId")
    @Mapping(target = "generatedAt", source = "reportInstanceEntity.createdTimestamp")
    @Mapping(target = "requestedBy.userId", source = "reportInstanceEntity.requestedBy")
    @Mapping(target = "requestedBy.name", source = "reportInstanceEntity.requestedByName")
    @Mapping(target = "name", expression = "java(getReportName(reportInstanceEntity))")
    @Mapping(target = "status.code", source = "reportInstanceEntity.generationStatus")
    @Mapping(target = "numberOfRecords", source = "reportInstanceEntity.noOfRecords")
    @Mapping(target = "isDownloadable", expression = "java(findIsDownloadable(reportInstanceEntity))")
    @Mapping(target = "errors", expression = "java(getErrors(reportInstanceEntity))")
    @Mapping(target = "reportParameters", source = "reportInstanceEntity.reportParameters",
        qualifiedByName = "parseJsonToMap")
    @Mapping(target = "retainUntil", source = "reportInstanceEntity.scheduledDeletionTimestamp")
    @Mapping(target = "businessUnits", source = "businessUnitEntities")
    ReportInstanceReports toReportInstanceReportsDto(ReportInstanceEntity reportInstanceEntity,
                                                     List<BusinessUnitEntity> businessUnitEntities);

    @Mapping(target = "id", source = "reportId")
    ReportReferenceReports reportToReportReferenceReport(ReportEntity reportEntity);

    default String getReportName(ReportInstanceEntity reportInstanceEntity) {
        return reportInstanceEntity.getReportName() != null ? reportInstanceEntity.getReportName() :
            reportInstanceEntity.getReport().getReportTitle();
    }

    default Boolean findIsDownloadable(ReportInstanceEntity reportInstanceEntity) {
        return ReportInstanceGenerationStatus.READY.equals(reportInstanceEntity.getGenerationStatus())
            && reportInstanceEntity.getReport().getSupportedFileTypes() != null
            && !reportInstanceEntity.getReport().getSupportedFileTypes().isEmpty();
    }

    default List<Map<String,Object>> getErrors(ReportInstanceEntity reportInstanceEntity) {
        if (!ReportInstanceGenerationStatus.ERROR.equals(reportInstanceEntity.getGenerationStatus())) {
            return null;
        }
        return List.of(Map.of(
            "operationId", reportInstanceEntity.getErrors().operationId(),
            "error", reportInstanceEntity.getErrors().error()));
    }


    @AfterMapping
    default void fillStatusDisplayName(@MappingTarget ReportInstanceReports responseDto) {
        responseDto.getStatus().setDisplayName(responseDto.getStatus().getCode().name());
    }
}
