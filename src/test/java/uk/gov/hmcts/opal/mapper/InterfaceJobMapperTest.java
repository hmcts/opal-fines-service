package uk.gov.hmcts.opal.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.opal.entity.InterfaceFileEntity;
import uk.gov.hmcts.opal.entity.InterfaceJobEntity;
import uk.gov.hmcts.opal.entity.InterfaceJobStatus;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsCreateItem;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsCreateResponseItem;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsFileSource;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsJobStatus;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsSummaryItem;

class InterfaceJobMapperTest {

    private final InterfaceJobMapper mapper = Mappers.getMapper(InterfaceJobMapper.class);

    @Test
    void toJobEntity_mapsRequestAndBusinessUnit() {
        LocalDateTime createdDateTime = LocalDateTime.of(2026, 7, 14, 10, 0);
        BusinessUnitEntity businessUnit = BusinessUnitEntity.builder().businessUnitId((short) 77).build();
        InterfaceJobsCreateItem request = InterfaceJobsCreateItem.builder()
            .businessUnitId((short) 77)
            .interfaceName("Auto Payments In")
            .createdDatetime(createdDateTime)
            .build();

        InterfaceJobEntity entity = mapper.toJobEntity(request, businessUnit);

        assertEquals(businessUnit, entity.getBusinessUnit());
        assertEquals("Auto Payments In", entity.getInterfaceName());
        assertEquals(InterfaceJobStatus.CREATED, entity.getStatus());
        assertEquals(createdDateTime, entity.getCreatedDateTime());
    }

    @Test
    void toFileEntity_mapsRequestAndJob() {
        LocalDateTime createdDateTime = LocalDateTime.of(2026, 7, 14, 10, 0);
        InterfaceJobEntity interfaceJob = InterfaceJobEntity.builder().interfaceJobId(123L).build();
        InterfaceJobsCreateItem request = InterfaceJobsCreateItem.builder()
            .fileName("auto-payments-in.dat")
            .source(InterfaceJobsFileSource.NATWEST)
            .records("[{\"account\":\"123\"}]")
            .createdDatetime(createdDateTime)
            .build();

        InterfaceFileEntity entity = mapper.toFileEntity(request, interfaceJob);

        assertEquals(interfaceJob, entity.getInterfaceJob());
        assertEquals("auto-payments-in.dat", entity.getFileName());
        assertEquals("NATWEST", entity.getSource());
        assertEquals("[{\"account\":\"123\"}]", entity.getRecords());
        assertEquals(createdDateTime, entity.getCreatedDateTime());
    }

    @Test
    void toCreateResponse_mapsInterfaceJobId() {
        InterfaceJobEntity interfaceJob = InterfaceJobEntity.builder().interfaceJobId(123L).build();

        InterfaceJobsCreateResponseItem response = mapper.toCreateResponse(interfaceJob);

        assertEquals(123L, response.getInterfaceJobId());
    }

    @Test
    void toSummaryResponse_mapsJobAndFileFields() {
        LocalDateTime completedDateTime = LocalDateTime.of(2026, 7, 1, 10, 30);
        LocalDateTime createdDateTime = LocalDateTime.of(2026, 7, 1, 10, 0);
        InterfaceJobEntity interfaceJob = InterfaceJobEntity.builder()
            .interfaceJobId(123L)
            .businessUnit(BusinessUnitEntity.builder().businessUnitName("Luton").build())
            .completedDateTime(completedDateTime)
            .createdDateTime(createdDateTime)
            .status(InterfaceJobStatus.COMPLETED)
            .build();
        InterfaceFileEntity interfaceFile = InterfaceFileEntity.builder()
            .interfaceFileId(456L)
            .fileName("auto-payments-in.dat")
            .source("NATWEST")
            .build();

        InterfaceJobsSummaryItem response = mapper.toSummaryResponse(interfaceJob, interfaceFile);

        assertEquals(123L, response.getInterfaceJobId());
        assertEquals(456L, response.getInterfaceFileId());
        assertEquals("auto-payments-in.dat", response.getFileName());
        assertEquals(InterfaceJobsFileSource.NATWEST, response.getSource());
        assertEquals("Luton", response.getBusinessUnitName());
        assertEquals(completedDateTime, response.getCompletedDatetime());
        assertEquals(createdDateTime, response.getCreatedDatetime());
        assertEquals(InterfaceJobsJobStatus.COMPLETED, response.getStatus());
    }
}
