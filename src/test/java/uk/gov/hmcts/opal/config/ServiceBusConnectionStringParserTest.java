package uk.gov.hmcts.opal.config;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;
import joptsimple.internal.Strings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import uk.gov.hmcts.opal.config.ServiceBusConnectionStringParser.ConnectionDetails;

public class ServiceBusConnectionStringParserTest {

    public static final String BASE_CONNECTION_STRING = "%s=%s;%s=%s;%s=%s";
    public static final String ENDPOINT_KEY = "Endpoint";
    public static final String KEY_VALUE_KEY = "SharedAccessKey";
    public static final String KEY_NAME_KEY = "SharedAccessKeyName";
    public static final String VALID_ENDPOINT = "sb://localhost/;";
    public static final String VALID_SHARED_ACCESS_KEY_NAME = "AccessKey";
    public static final String VALID_SHARED_ACCESS_KEY_VALUE = "keyValue";

    static Stream<Arguments> provideBlankEndpoint() {
        return Stream.of(Arguments.of(Strings.EMPTY, VALID_ENDPOINT),
            Arguments.of(ENDPOINT_KEY, Strings.EMPTY));
    }

    static Stream<Arguments> provideBlankSharedAccessKeyName() {
        return Stream.of(Arguments.of(Strings.EMPTY, VALID_SHARED_ACCESS_KEY_NAME),
            Arguments.of(KEY_NAME_KEY, Strings.EMPTY));
    }

    static Stream<Arguments> provideBlankSharedAccessKeyValue() {
        return Stream.of(Arguments.of(Strings.EMPTY, VALID_SHARED_ACCESS_KEY_VALUE),
            Arguments.of(KEY_VALUE_KEY, Strings.EMPTY));
    }

    @Test
    public void parse_validConnectionString_returnsConnectionDetails() {
        ConnectionDetails parsed = ServiceBusConnectionStringParser.parse(
            String.format(BASE_CONNECTION_STRING,
                ENDPOINT_KEY, VALID_ENDPOINT,
                KEY_NAME_KEY, VALID_SHARED_ACCESS_KEY_NAME,
                KEY_VALUE_KEY, VALID_SHARED_ACCESS_KEY_VALUE));
        assertThat(parsed).hasNoNullFieldsOrProperties();
    }

    @ParameterizedTest
    @NullAndEmptySource
    public void parse_noConnectionStringPassed_throwsIllegalArgumentException(String connectionString) {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> ServiceBusConnectionStringParser.parse(connectionString));
        assertThat(ex).hasMessageContaining("must not be blank");
    }

    @ParameterizedTest
    @MethodSource("provideBlankEndpoint")
    public void parse_endpointIsBlank_throwsIllegalArgumentException(String key, String value) {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> ServiceBusConnectionStringParser.parse(
                String.format(BASE_CONNECTION_STRING,
                    key, value,
                    KEY_NAME_KEY, VALID_SHARED_ACCESS_KEY_NAME,
                    KEY_VALUE_KEY, VALID_SHARED_ACCESS_KEY_VALUE)));
        assertThat(ex).hasMessageContaining("missing endpoint segment");
    }

    @Test
    public void parse_invalidUriInEndpoint_throwsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> ServiceBusConnectionStringParser.parse(
                String.format(BASE_CONNECTION_STRING,
                    ENDPOINT_KEY, "invalid uri",
                    KEY_NAME_KEY, VALID_SHARED_ACCESS_KEY_NAME,
                    KEY_VALUE_KEY, VALID_SHARED_ACCESS_KEY_VALUE)));
        assertThat(ex).hasMessageContaining("invalid uri");
    }

    @Test
    public void parse_noHostInEndpoint_throwsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> ServiceBusConnectionStringParser.parse(
                String.format(BASE_CONNECTION_STRING,
                    ENDPOINT_KEY, "missingHost",
                    KEY_NAME_KEY, VALID_SHARED_ACCESS_KEY_NAME,
                    KEY_VALUE_KEY, VALID_SHARED_ACCESS_KEY_VALUE)));
        assertThat(ex).hasMessageContaining("missing host");
    }

    @ParameterizedTest
    @MethodSource("provideBlankSharedAccessKeyName")
    public void parse_noSharedAccessKeyName_throwsIllegalArgumentException(String key, String value) {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> ServiceBusConnectionStringParser.parse(
                String.format(BASE_CONNECTION_STRING,
                    ENDPOINT_KEY, VALID_ENDPOINT,
                    key, value,
                    KEY_VALUE_KEY, VALID_SHARED_ACCESS_KEY_VALUE)));
        assertThat(ex).hasMessageContaining("missing " + KEY_NAME_KEY);
    }

    @ParameterizedTest
    @MethodSource("provideBlankSharedAccessKeyValue")
    public void parse_noSharedAccessKey_throwsIllegalArgumentException(String key, String value) {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> ServiceBusConnectionStringParser.parse(
                String.format(BASE_CONNECTION_STRING,
                    ENDPOINT_KEY, VALID_ENDPOINT,
                    KEY_NAME_KEY, VALID_SHARED_ACCESS_KEY_NAME,
                    key, value)));
        assertThat(ex).hasMessageContaining("missing " + KEY_VALUE_KEY);
    }

}