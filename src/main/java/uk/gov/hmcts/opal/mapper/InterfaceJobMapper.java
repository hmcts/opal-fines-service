package uk.gov.hmcts.opal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.hmcts.opal.entity.InterfaceFileEntity;
import uk.gov.hmcts.opal.entity.InterfaceJobEntity;
import uk.gov.hmcts.opal.entity.InterfaceJobStatus;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsCreateItem;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsCreateResponseItem;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsFileSource;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsJobStatus;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsSummaryItem;

@Mapper(componentModel = "spring")
public interface InterfaceJobMapper {

    @Mapping(target = "interfaceJobId", ignore = true)
    @Mapping(target = "businessUnit", source = "businessUnit")
    @Mapping(target = "interfaceName", source = "request.interfaceName")
    @Mapping(target = "status", constant = "CREATED")
    @Mapping(target = "createdDateTime", source = "request.createdDatetime")
    @Mapping(target = "startedDateTime", ignore = true)
    @Mapping(target = "completedDateTime", ignore = true)
    @Mapping(target = "interfaceFiles", ignore = true)
    InterfaceJobEntity toJobEntity(InterfaceJobsCreateItem request, BusinessUnitEntity businessUnit);

    @Mapping(target = "interfaceFileId", ignore = true)
    @Mapping(target = "interfaceJob", source = "interfaceJob")
    @Mapping(target = "fileName", source = "request.fileName")
    @Mapping(target = "createdDateTime", source = "request.createdDatetime")
    @Mapping(target = "source", source = "request.source")
    @Mapping(target = "records", source = "request.records")
    @Mapping(target = "recordCount", ignore = true)
    InterfaceFileEntity toFileEntity(InterfaceJobsCreateItem request, InterfaceJobEntity interfaceJob);

    @Mapping(target = "interfaceJobId", source = "interfaceJobId")
    InterfaceJobsCreateResponseItem toCreateResponse(InterfaceJobEntity interfaceJob);

    @Mapping(target = "interfaceJobId", source = "job.interfaceJobId")
    @Mapping(target = "interfaceFileId", source = "file.interfaceFileId")
    @Mapping(target = "fileName", source = "file.fileName")
    @Mapping(target = "source", source = "file.source")
    @Mapping(target = "businessUnitName", source = "job.businessUnit.businessUnitName")
    @Mapping(target = "completedDatetime", source = "job.completedDateTime")
    @Mapping(target = "createdDatetime", source = "job.createdDateTime")
    @Mapping(target = "status", source = "job.status")
    InterfaceJobsSummaryItem toSummaryResponse(InterfaceJobEntity job, InterfaceFileEntity file);

    default InterfaceJobsFileSource toFileSource(String source) {
        return InterfaceJobsFileSource.fromValue(source);
    }

    default String toFileSource(InterfaceJobsFileSource source) {
        return source == null ? null : source.getValue();
    }

    default InterfaceJobsJobStatus toJobStatus(InterfaceJobStatus status) {
        return InterfaceJobsJobStatus.fromValue(status.name());
    }
}
