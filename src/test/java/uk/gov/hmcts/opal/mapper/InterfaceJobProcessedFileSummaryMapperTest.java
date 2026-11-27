package uk.gov.hmcts.opal.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.opal.entity.InterfaceFileEntity;
import uk.gov.hmcts.opal.entity.InterfaceJobProcessedFileSummaryEntity;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsMessage;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsMessageGroup;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsMessageType;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsProcessedFileSummaryResponse;

class InterfaceJobProcessedFileSummaryMapperTest {

    private static final long INTERFACE_MESSAGE_ID = 11L;

    private final InterfaceJobProcessedFileSummaryMapper mapper =
        Mappers.getMapper(InterfaceJobProcessedFileSummaryMapper.class);

    @ParameterizedTest
    @CsvSource({"0, 0.00", "1, 0.01", "1000, 10.00", "12345, 123.45"})
    void toResponseConvertsCompletedSummaryPence(BigDecimal pence, BigDecimal pounds) {
        InterfaceJobProcessedFileSummaryEntity summary = mock(InterfaceJobProcessedFileSummaryEntity.class);
        when(summary.getInterfaceFileName()).thenReturn("completed-payments.dat");
        when(summary.getSource()).thenReturn("NATWEST");
        when(summary.getTotalAmount()).thenReturn(pence);
        when(summary.getTotalRecords()).thenReturn((short) 4);
        when(summary.getTotalErrors()).thenReturn(2L);
        InterfaceJobsMessageGroup messageGroup = new InterfaceJobsMessageGroup()
            .messageText("records_rejected")
            .messages(List.of(new InterfaceJobsMessage().interfaceMessagesId(INTERFACE_MESSAGE_ID)));

        InterfaceJobsProcessedFileSummaryResponse response = mapper.toResponse(
            summary, "Luton", List.of(messageGroup));

        assertEquals("completed-payments.dat", response.getFileName());
        assertEquals("NATWEST", response.getSource());
        assertEquals("Luton", response.getBusinessUnitName());
        assertEquals(0, pounds.compareTo(response.getTotalAmount()));
        assertEquals(pence, summary.getTotalAmount());
        assertEquals((short) 4, response.getTotalRecords());
        assertEquals(2L, response.getTotalErrors());
        assertEquals(List.of(messageGroup), response.getInterfaceMessages());
    }

    @ParameterizedTest
    @CsvSource({"0, 0.00", "1, 0.01", "1000, 10.00", "12345, 123.45"})
    void toResponseConvertsPenceAndPreservesMessages(BigDecimal pence, BigDecimal pounds) {
        // Arrange
        InterfaceFileEntity file = InterfaceFileEntity.builder()
            .fileName("payments.dat")
            .source("NATWEST")
            .totalAmount(pence)
            .recordCount((short) 2)
            .build();

        InterfaceJobsMessage message = new InterfaceJobsMessage()
            .interfaceMessagesId(INTERFACE_MESSAGE_ID)
            .messageData(Map.of("record", 3, "value", new BigDecimal("123.45"),
                "court_amount", new BigDecimal("12.34")))
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
        assertEquals(0, pounds.compareTo(response.getTotalAmount()));
        assertEquals(pence, file.getTotalAmount());
        assertEquals((short) 2, response.getTotalRecords());
        assertEquals(1L, response.getTotalErrors());
        assertEquals(List.of(messageGroup), response.getInterfaceMessages());
        assertEquals(Map.of("record", 3, "value", new BigDecimal("123.45"),
            "court_amount", new BigDecimal("12.34")),
            response.getInterfaceMessages().getFirst().getMessages().getFirst().getMessageData());
    }

    @Test
    void toResponsePreservesNullTotal() {
        InterfaceFileEntity file = InterfaceFileEntity.builder().build();

        InterfaceJobsProcessedFileSummaryResponse response = mapper.toResponse(file, "Luton", 0L, List.of());

        assertNull(response.getTotalAmount());
    }

    @Test
    void toResponsePreservesNullCompletedSummaryTotal() {
        InterfaceJobProcessedFileSummaryEntity summary = mock(InterfaceJobProcessedFileSummaryEntity.class);

        InterfaceJobsProcessedFileSummaryResponse response = mapper.toResponse(summary, "Luton", List.of());

        assertNull(response.getTotalAmount());
    }
}
