package uk.gov.hmcts.opal.util;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.PathNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "opal.JsonPathUtil")
public class JsonPathUtil {

    public static DocContext createDocContext(String document, String errorSource) {
        Configuration config = Configuration.defaultConfiguration().addOptions(Option.DEFAULT_PATH_LEAF_TO_NULL);
        try {
            return new DocContext(JsonPath.parse(document, config), document);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                "Cannot create a JSON Document Context from an empty or null String. See " + errorSource,
                exception);
        }
    }

    public static class DocContext {
        private final DocumentContext documentContext;
        private final String originalDocument;

        public DocContext(DocumentContext documentContext, String originalDocument) {
            this.documentContext = documentContext;
            this.originalDocument = originalDocument;
        }

        public <T> T read(String path) {
            try {
                return documentContext.read(path);
            } catch (PathNotFoundException exception) {
                // All this is required because the PathNotFoundException suppresses the Stack Trace.
                throw new RuntimeException(exception.getMessage());
            }
        }

        public <T> T readOrNull(String path) {
            try {
                return documentContext.read(path);
            } catch (PathNotFoundException pathNotFoundException) {
                return null;
            }
        }
    }

    public static String safeReadString(DocContext docContext, String path, String defaultValue) {
        try {
            Object value = docContext.read(path);
            if (value == null) {
                return defaultValue;
            }
            return String.valueOf(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static LocalDate safeReadLocalDate(DocContext docContext, String path) {
        try {
            String stringValue = safeReadString(docContext, path, null);
            if (stringValue == null || stringValue.isBlank()) {
                return null;
            }
            return LocalDate.parse(stringValue);
        } catch (Exception e) {
            return null;
        }
    }

    public static Boolean safeReadBoolean(DocContext docContext, String path, Boolean defaultValue) {
        try {
            Object value = docContext.read(path);
            if (value == null) {
                return defaultValue;
            }
            if (value instanceof Boolean) {
                return (Boolean) value;
            }
            String stringValue = String.valueOf(value).trim();
            if (stringValue.equalsIgnoreCase("true") || stringValue.equalsIgnoreCase("y")
                || stringValue.equalsIgnoreCase("yes")) {
                return Boolean.TRUE;
            }
            if (stringValue.equalsIgnoreCase("false") || stringValue.equalsIgnoreCase("n")
                || stringValue.equalsIgnoreCase("no")) {
                return Boolean.FALSE;
            }
            return defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static BigDecimal safeReadBigDecimal(DocContext docContext, String path) {
        try {
            Object value = docContext.read(path);
            if (value == null) {
                return null;
            }
            if (value instanceof Number) {
                return new BigDecimal(String.valueOf(value));
            }
            return new BigDecimal(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
    }
}
