package uk.gov.hmcts.opal.util;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import java.nio.charset.StandardCharsets;
import org.apache.qpid.jms.message.JmsBytesMessage;

public class QueueUtil {

    private QueueUtil() {

    }

    public static String getMessageAsString(Message message) throws JMSException {
        return switch (message) {
            case TextMessage textMessage -> textMessage.getText();
            case JmsBytesMessage bytesMessage -> {
                byte[] body = bytesMessage.getBody(byte[].class);
                yield body == null ? null : new String(body, StandardCharsets.UTF_8);
            }
            default -> throw new IllegalArgumentException(
                "Message must be of type TextMessage or JmsBytesMessage"
            );
        };
    }
}
