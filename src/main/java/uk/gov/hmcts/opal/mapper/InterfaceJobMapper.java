package uk.gov.hmcts.opal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.hmcts.opal.entity.InterfaceFileEntity;
import uk.gov.hmcts.opal.entity.InterfaceJobEntity;
import uk.gov.hmcts.opal.entity.InterfaceJobStatus;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsFileSource;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsJobStatus;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsSummaryItem;

@Mapper(componentModel = "spring")
public interface InterfaceJobMapper {

    @Mapping(target = "interfaceJobId", source = "job.interfaceJobId")
    @Mapping(target = "interfaceFileId", source = "file.interfaceFileId")
    @Mapping(target = "fileName", source = "file.fileName")
    @Mapping(target = "source", source = "file.source")
    @Mapping(target = "businessUnitName", source = "job.businessUnit.businessUnitName")
    @Mapping(target = "completedDatetime", source = "job.completedDateTime")
    @Mapping(target = "createdDatetime", source = "job.createdDateTime")
    @Mapping(target = "status", source = "job.status")
    InterfaceJobsSummaryItem toSummaryResponse(InterfaceJobEntity job, InterfaceFileEntity file);

    default InterfaceJobsFileSource toInterfaceJobsFileSource(String source) {
        return InterfaceJobsFileSource.fromValue(source);
    }

    default InterfaceJobsJobStatus toInterfaceJobsJobStatus(InterfaceJobStatus status) {
        return InterfaceJobsJobStatus.fromValue(status.name());
    }
}
