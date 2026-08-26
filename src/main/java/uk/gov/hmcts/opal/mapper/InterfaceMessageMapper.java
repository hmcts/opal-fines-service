package uk.gov.hmcts.opal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import uk.gov.hmcts.opal.entity.InterfaceMessageEntity;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsMessage;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsMessageType;
import uk.gov.hmcts.opal.mapper.helper.JsonMapperHelper;

@Mapper(componentModel = "spring", uses = JsonMapperHelper.class)
public interface InterfaceMessageMapper {

    @Mapping(target = "interfaceMessagesId", source = "interfaceMessageId")
    @Mapping(target = "messageData", source = "messageData", qualifiedByName = "parseJsonToMap")
    @Mapping(target = "messageType", source = "messageType", qualifiedByName = "toMessageType")
    InterfaceJobsMessage toMessage(InterfaceMessageEntity message);

    @Named("toMessageType")
    default InterfaceJobsMessageType toMessageType(String messageType) {
        return messageType == null ? null : InterfaceJobsMessageType.fromValue(messageType);
    }
}
