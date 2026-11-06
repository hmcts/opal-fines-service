package uk.gov.hmcts.opal.service.refdata.lja;

import uk.gov.hmcts.opal.service.refdata.EmulatorUtil;

/**
 * Small command-line utility for publishing a text message to the local Service Bus emulator.
 *
 * <p>
 * Usage:
 *   ./gradlew publishServiceBusMessage --args='topic.refdata "{\"hello\":\"world\"}" session-1'
 *   ./gradlew publishServiceBusMessage --args='topic.refdata @message.json session-1'
 */
public final class LjaEmulatorMessagePublisher {

    private static final String DEFAULT_CONNECTION_STRING =
        "Endpoint=sb://localhost;SharedAccessKeyName=RootManageSharedAccessKey;"
            + "SharedAccessKey=SAS_KEY_VALUE;UseDevelopmentEmulator=true;";

    private LjaEmulatorMessagePublisher() {
    }

    public static void main(String[] args) throws Exception {

        final Short localJusticeAreaId = (short)401;
        String payload = LjaMessageBuilder.buildRefDataLjaMessage("LJA", 1, true,
            String.format("%04d", localJusticeAreaId),
            "Updated LJA", "2027-03-04",
            "New address NovaChips 171",
            "New address line 2SR1910",
            "New address line 3",
            "New address line 4",
            "New address line 5",
            "NE1 2BB");

        String connectionString = System.getenv().getOrDefault(
            "SERVICEBUS_CONNECTION_STRING", DEFAULT_CONNECTION_STRING);

        try (EmulatorUtil emulator = new EmulatorUtil(connectionString)) {
            emulator.publishMessageToTopic(payload, "ref-data-session-id");
        }
    }
}
