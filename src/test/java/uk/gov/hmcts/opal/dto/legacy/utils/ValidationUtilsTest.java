package uk.gov.hmcts.opal.dto.legacy.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ValidationUtilsTest {
    @Nested
    @DisplayName("hasExactlyOneNonNull(...)")
    class HasExactlyOneNonNull {

        @Test
        void returnsFalse_whenAllNull() {
            assertFalse(ValidationUtils.hasExactlyOneNonNull(null, null, null));
        }

        @Test
        void returnsTrue_whenExactlyOneNonNull_first() {
            assertTrue(ValidationUtils.hasExactlyOneNonNull("x", null, null));
        }

        @Test
        void returnsTrue_whenExactlyOneNonNull_middle() {
            assertTrue(ValidationUtils.hasExactlyOneNonNull(null, 123, null));
        }

        @Test
        void returnsTrue_whenExactlyOneNonNull_last() {
            assertTrue(ValidationUtils.hasExactlyOneNonNull(null, null, new Object()));
        }

        @Test
        void returnsFalse_whenTwoNonNull() {
            assertFalse(ValidationUtils.hasExactlyOneNonNull("x", 1, null));
            assertFalse(ValidationUtils.hasExactlyOneNonNull(null, 1, new Object()));
            assertFalse(ValidationUtils.hasExactlyOneNonNull("x", null, new Object()));
        }

        @Test
        void returnsFalse_whenMoreThanTwoNonNull() {
            assertFalse(ValidationUtils.hasExactlyOneNonNull("x", 1, new Object(), Boolean.TRUE));
        }

        @Test
        void supportsMixedTypes() {
            record R(int a) {

            }

            assertTrue(ValidationUtils.hasExactlyOneNonNull(new R(7)));
            assertFalse(ValidationUtils.hasExactlyOneNonNull(new R(7), "also non-null"));
        }

        @Test
        void returnsFalse_whenEmptyInput() {
            // Varargs is empty → zero non-nulls → false
            assertFalse(ValidationUtils.hasExactlyOneNonNull());
        }
    }
}