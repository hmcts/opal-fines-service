package uk.gov.hmcts.opal.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.hmcts.opal.entity.InterfaceFileEntity;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsMessageGroup;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsProcessedFileSummaryResponse;

@Mapper(componentModel = "spring")
public interface InterfaceJobProcessedFileSummaryMapper {

    @Mapping(target = "fileName", source = "file.fileName")
    @Mapping(target = "source", source = "file.source")
    @Mapping(target = "totalAmount", source = "file.totalAmount")
    @Mapping(target = "totalRecords", source = "file.recordCount")
    @Mapping(target = "totalErrors", source = "totalErrors")
    @Mapping(target = "businessUnitName", source = "businessUnitName")
    @Mapping(target = "interfaceMessages", source = "messages")
    InterfaceJobsProcessedFileSummaryResponse toResponse(
        InterfaceFileEntity file,
        String businessUnitName,
        Long totalErrors,
        List<InterfaceJobsMessageGroup> messages);
}
