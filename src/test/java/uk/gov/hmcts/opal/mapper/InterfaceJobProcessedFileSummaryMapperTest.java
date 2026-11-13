package uk.gov.hmcts.opal.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.opal.entity.InterfaceFileEntity;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsMessage;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsMessageGroup;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsMessageType;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsProcessedFileSummaryResponse;

class InterfaceJobProcessedFileSummaryMapperTest {

    private static final long INTERFACE_MESSAGE_ID = 11L;

    private final InterfaceJobProcessedFileSummaryMapper mapper =
        Mappers.getMapper(InterfaceJobProcessedFileSummaryMapper.class);

    @Test
    void toResponseMapsSummaryAndMessages() {
        // Arrange
        InterfaceFileEntity file = InterfaceFileEntity.builder()
            .fileName("payments.dat")
            .source("NATWEST")
            .totalAmount(new BigDecimal("10.00"))
            .recordCount((short) 2)
            .build();

        InterfaceJobsMessage message = new InterfaceJobsMessage()
            .interfaceMessagesId(INTERFACE_MESSAGE_ID)
            .messageData(Map.of("record", 3))
            .messageType(InterfaceJobsMessageType.ERROR);

        InterfaceJobsMessageGroup messageGroup = new InterfaceJobsMessageGroup()
            .messageText("bad record")
            .messages(List.of(message));

        // Act
        InterfaceJobsProcessedFileSummaryResponse response = mapper.toResponse(
            file, "Luton", 1L, List.of(messageGroup));

        // Assert
        assertEquals("payments.dat", response.getFileName());
        assertEquals("NATWEST", response.getSource());
        assertEquals("Luton", response.getBusinessUnitName());
        assertEquals(new BigDecimal("10.00"), response.getTotalAmount());
        assertEquals((short) 2, response.getTotalRecords());
        assertEquals(1L, response.getTotalErrors());
        assertEquals(List.of(messageGroup), response.getInterfaceMessages());
    }
}
