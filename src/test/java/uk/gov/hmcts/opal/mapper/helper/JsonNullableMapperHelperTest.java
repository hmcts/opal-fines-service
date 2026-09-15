package uk.gov.hmcts.opal.mapper.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.opal.mapper.helper.JsonNullableMapperHelper.fromJsonNullable;
import static uk.gov.hmcts.opal.mapper.helper.JsonNullableMapperHelper.toJsonNullable;
import static uk.gov.hmcts.opal.mapper.helper.JsonNullableMapperHelper.toJsonNullableOrUndefined;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;

class JsonNullableMapperHelperTest {

    @Nested
    class ToJsonNullable {

        @Test
        void shouldWrapValue() {
            JsonNullable<String> result = toJsonNullable("value");

            assertEquals("value", result.get());
        }

        @Test
        void shouldPreserveExplicitNull() {
            JsonNullable<String> result = toJsonNullable(null);

            assertTrue(result.isPresent());
            assertNull(result.orElse("fallback"));
        }
    }

    @Nested
    class ToJsonNullableOrUndefined {

        @Test
        void shouldWrapValue() {
            JsonNullable<String> result = toJsonNullableOrUndefined("value");

            assertEquals("value", result.get());
        }

        @Test
        void shouldReturnUndefinedForNull() {
            JsonNullable<String> result = toJsonNullableOrUndefined(null);

            assertFalse(result.isPresent());
        }
    }

    @Nested
    class FromJsonNullable {

        @Test
        void shouldReturnWrappedValue() {
            assertEquals("value", fromJsonNullable(JsonNullable.of("value")));
        }

        @Test
        void shouldReturnNullForUndefinedValue() {
            assertNull(fromJsonNullable(JsonNullable.undefined()));
        }

        @Test
        void shouldReturnNullForNullWrapper() {
            assertNull(fromJsonNullable(null));
        }
    }
}
