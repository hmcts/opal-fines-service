package uk.gov.hmcts.opal.dto.legacy.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.opal.common.legacy.service.GatewayService.Response;

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

    @Nested
    @DisplayName("checkResponseForError(...)")
    class CheckResponseForError {

        @Test
        void logsSuccess_whenResponseIsSuccessful() {
            var appender = attachAppender();

            try {
                var response = new Response<>(HttpStatus.OK, "body", null, null);

                ValidationUtils.checkResponseForError(response, "testMethod");

                assertThat(appender.list)
                    .anySatisfy(event -> {
                        assertThat(event.getLevel()).isEqualTo(Level.INFO);
                        assertThat(event.getFormattedMessage()).isEqualTo(":testMethod: legacy success.");
                    });
            } finally {
                detachAppender(appender);
            }
        }

        @Test
        void logsLegacyFailureBody_whenResponseIsLegacyFailure() {
            var appender = attachAppender();

            try {
                var response = new Response<>(HttpStatus.INTERNAL_SERVER_ERROR,
                    null, "<error/>", null);

                ValidationUtils.checkResponseForError(response, "testMethod");

                assertThat(appender.list)
                    .anySatisfy(event -> {
                        assertThat(event.getLevel()).isEqualTo(Level.ERROR);
                        assertThat(event.getFormattedMessage())
                            .isEqualTo(":testMethod: legacy error HTTP 500 INTERNAL_SERVER_ERROR");
                    })
                    .anySatisfy(event -> {
                        assertThat(event.getLevel()).isEqualTo(Level.ERROR);
                        assertThat(event.getFormattedMessage())
                            .isEqualTo(":testMethod: legacy failure body:\n<error/>");
                    });
            } finally {
                detachAppender(appender);
            }
        }

        @Test
        void logsException_whenResponseContainsException() {
            var appender = attachAppender();

            try {
                var exception = new RuntimeException("gateway boom");
                var response = new Response<String>(HttpStatus.BAD_GATEWAY, exception, null);

                ValidationUtils.checkResponseForError(response, "testMethod");

                assertThat(appender.list)
                    .anySatisfy(event -> {
                        assertThat(event.getLevel()).isEqualTo(Level.ERROR);
                        assertThat(event.getFormattedMessage())
                            .isEqualTo(":testMethod: legacy error HTTP 502 BAD_GATEWAY");
                    })
                    .anySatisfy(event -> {
                        assertThat(event.getLevel()).isEqualTo(Level.ERROR);
                        assertThat(event.getFormattedMessage()).isEqualTo(":testMethod: exception:");
                        assertThat(event.getThrowableProxy().getMessage()).isEqualTo("gateway boom");
                    });
            } finally {
                detachAppender(appender);
            }
        }

        private ListAppender<ILoggingEvent> attachAppender() {
            Logger logger = (Logger) LoggerFactory.getLogger("opal.LegacyValidationUtils");
            var appender = new ListAppender<ILoggingEvent>();
            appender.start();
            logger.addAppender(appender);
            return appender;
        }

        private void detachAppender(ListAppender<ILoggingEvent> appender) {
            Logger logger = (Logger) LoggerFactory.getLogger("opal.LegacyValidationUtils");
            logger.detachAppender(appender);
        }

    }

}