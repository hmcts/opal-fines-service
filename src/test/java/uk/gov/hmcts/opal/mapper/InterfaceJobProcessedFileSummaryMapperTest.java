package uk.gov.hmcts.opal.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.opal.entity.InterfaceJobProcessedFileSummaryEntity;
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
        InterfaceJobProcessedFileSummaryEntity summary = mock(InterfaceJobProcessedFileSummaryEntity.class);
        when(summary.getInterfaceFileName()).thenReturn("payments.dat");
        when(summary.getSource()).thenReturn("NATWEST");
        when(summary.getTotalAmount()).thenReturn(new BigDecimal("10.00"));
        when(summary.getTotalRecords()).thenReturn((short) 2);
        when(summary.getTotalErrors()).thenReturn(1L);

        InterfaceJobsMessage message = new InterfaceJobsMessage()
            .interfaceMessagesId(INTERFACE_MESSAGE_ID)
            .messageData(Map.of("record", 3))
            .messageType(InterfaceJobsMessageType.ERROR);

        InterfaceJobsMessageGroup messageGroup = new InterfaceJobsMessageGroup()
            .messageText("bad record")
            .messages(List.of(message));

        // Act
        InterfaceJobsProcessedFileSummaryResponse response = mapper.toResponse(
            summary, "Luton", List.of(messageGroup));

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
