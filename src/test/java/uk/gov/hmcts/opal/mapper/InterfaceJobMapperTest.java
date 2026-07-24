package uk.gov.hmcts.opal.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.opal.entity.InterfaceFileEntity;
import uk.gov.hmcts.opal.entity.InterfaceJobEntity;
import uk.gov.hmcts.opal.entity.InterfaceJobStatus;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsFileSource;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsJobStatus;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsSummaryItem;

class InterfaceJobMapperTest {

    private final InterfaceJobMapper mapper = Mappers.getMapper(InterfaceJobMapper.class);

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
