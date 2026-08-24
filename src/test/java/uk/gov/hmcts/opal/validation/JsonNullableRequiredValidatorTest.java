package uk.gov.hmcts.opal.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openapitools.jackson.nullable.JsonNullable;

class JsonNullableRequiredValidatorTest {

    private final JsonNullableRequiredValidator validator = new JsonNullableRequiredValidator();

    @Nested
    class IsValid {

        @ParameterizedTest
        @MethodSource("validationCases")
        void whenValueHasDifferentJsonStates_thenValidatesPropertyPresence(
            JsonNullable<?> value, boolean expectedValid) {
            assertEquals(expectedValid, validator.isValid(value, null));
        }

        private static Stream<Arguments> validationCases() {
            return Stream.of(
                Arguments.of(null, false),
                Arguments.of(JsonNullable.undefined(), false),
                Arguments.of(JsonNullable.of(null), true),
                Arguments.of(JsonNullable.of("postcode"), true)
            );
        }
    }
}
