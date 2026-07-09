package uk.gov.hmcts.opal.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.opal.service.report.FileType.CSV;
import static uk.gov.hmcts.opal.service.report.FileType.JSON;
import static uk.gov.hmcts.opal.service.report.FileType.PDF;
import static uk.gov.hmcts.opal.service.report.FileType.XML;
import static uk.gov.hmcts.opal.util.ReportContentTypeUtil.resolveFileType;

import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.opal.service.report.FileType;

class ReportContentTypeUtilTest {

    @Nested
    class ResolveFileType {

        static Stream<Arguments> supportedHeaders() {
            return Stream.of(
                Arguments.of(null, JSON),
                Arguments.of("application/json", JSON),
                Arguments.of("*/*", JSON),
                Arguments.of("application/csv", CSV),
                Arguments.of("application/pdf", PDF),
                Arguments.of("application/xml", XML)
            );
        }

        @ParameterizedTest
        @MethodSource("supportedHeaders")
        void whenHeaderIsSupported_returnsExpectedFileType_happyPath(String acceptHeader, FileType expectedFileType) {
            FileType actual = resolveFileType(acceptHeader);
            assertEquals(expectedFileType, actual);
        }

        @Test
        void whenHeaderIsUnsupported_returnsNull_sadPath() {
            assertNull(resolveFileType("text/plain"));
        }
    }
}
