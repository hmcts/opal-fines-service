package uk.gov.hmcts.opal.mapper.testing;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.hmcts.opal.entity.InterfaceJobEntity;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsCreateResponseItemTestingSupport;

@Mapper(componentModel = "spring")
public interface TestingSupportInterfaceJobMapper {
    @Mapping(target = "interfaceFileId",
        expression = "java(interfaceJob.getInterfaceFiles().getFirst().getInterfaceFileId())")
    InterfaceJobsCreateResponseItemTestingSupport toCreateResponseItem(InterfaceJobEntity interfaceJob);
}
