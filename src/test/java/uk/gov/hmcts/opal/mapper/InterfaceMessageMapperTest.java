package uk.gov.hmcts.opal.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import uk.gov.hmcts.opal.config.JacksonCompatibilityConfiguration;
import uk.gov.hmcts.opal.entity.InterfaceMessageEntity;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsMessage;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsMessageType;
import uk.gov.hmcts.opal.mapper.helper.JsonMapperHelper;

@SpringJUnitConfig(classes = {JacksonCompatibilityConfiguration.class, InterfaceMessageMapperImpl.class,
    JsonMapperHelper.class})
class InterfaceMessageMapperTest {

    private static final long INTERFACE_MESSAGE_ID = 11L;

    @Autowired
    private InterfaceMessageMapper mapper;

    @Test
    void toMessageMapsFieldsAndJson() {
        // Arrange
        InterfaceMessageEntity message = InterfaceMessageEntity.builder()
            .interfaceMessageId(INTERFACE_MESSAGE_ID)
            .messageType("Error")
            .messageText("bad record")
            .messageData("{\"record\":3}")
            .build();

        // Act
        InterfaceJobsMessage result = mapper.toMessage(message);

        // Assert
        assertEquals(INTERFACE_MESSAGE_ID, result.getInterfaceMessagesId());
        assertEquals(InterfaceJobsMessageType.ERROR, result.getMessageType());
        assertEquals(Map.of("record", 3), result.getMessageData());
    }

    @Test
    void toMessageRejectsInvalidMessageType() {
        InterfaceMessageEntity message = InterfaceMessageEntity.builder()
            .messageType("Invalid")
            .messageText("invalid type")
            .messageData("{}")
            .build();

        assertThrows(IllegalArgumentException.class, () -> mapper.toMessage(message));
    }
}
