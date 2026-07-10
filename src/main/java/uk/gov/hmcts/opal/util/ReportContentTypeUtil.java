package uk.gov.hmcts.opal.util;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PDF;
import static org.springframework.http.MediaType.APPLICATION_XML;
import static org.springframework.http.MediaType.parseMediaType;
import static uk.gov.hmcts.opal.service.report.FileType.CSV;
import static uk.gov.hmcts.opal.service.report.FileType.JSON;
import static uk.gov.hmcts.opal.service.report.FileType.PDF;
import static uk.gov.hmcts.opal.service.report.FileType.XML;

import org.springframework.http.MediaType;
import uk.gov.hmcts.opal.service.report.FileType;

public final class ReportContentTypeUtil {

    private static final MediaType CSV_MEDIA_TYPE = parseMediaType("application/csv");

    private ReportContentTypeUtil() {
    }

    public static FileType resolveFileType(String acceptHeader) {
        if (acceptHeader == null || acceptHeader.isBlank()) {
            return JSON;
        }

        for (MediaType mediaType : MediaType.parseMediaTypes(acceptHeader)) {
            if (mediaType.isWildcardType() && mediaType.isWildcardSubtype()) {
                return JSON;
            }
            if (mediaType.isCompatibleWith(APPLICATION_PDF)) {
                return PDF;
            }
            if (mediaType.isCompatibleWith(CSV_MEDIA_TYPE)) {
                return CSV;
            }
            if (mediaType.isCompatibleWith(APPLICATION_XML)) {
                return XML;
            }
            if (mediaType.isCompatibleWith(APPLICATION_JSON)) {
                return JSON;
            }
        }

        return null;
    }
}
