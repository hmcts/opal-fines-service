package uk.gov.hmcts.opal.util;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.PathNotFoundException;
import java.math.BigDecimal;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "opal.JsonPathUtil")
public class JsonPathUtil {

    public static DocContext createDocContext(String document, String errorSource) {
        Configuration config = Configuration.defaultConfiguration().addOptions(Option.DEFAULT_PATH_LEAF_TO_NULL);
        try {
            return new DocContext(JsonPath.parse(document, config), document);
        } catch (IllegalArgumentException iae) {
            throw new IllegalArgumentException(
                "Cannot create a JSON Document Context from an empty or null String. See " + errorSource, iae);
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
            } catch (PathNotFoundException pnfe) {
                // All this is required because the PathNotFoundException suppresses the Stack Trace.
                throw new RuntimeException(pnfe.getMessage());
            }
        }

        public <T> T readOrNull(String path) {
            try {
                return documentContext.read(path);
            } catch (PathNotFoundException pnfe) {
                return null;
            }
        }
    }

    public static String safeReadString(DocContext ctx, String path, String def) {
        try {
            Object v = ctx.read(path);
            if (v == null) {
                return def;
            }
            return String.valueOf(v);
        } catch (Exception e) {
            return def;
        }
    }

    public static java.time.LocalDate safeReadLocalDate(DocContext ctx, String path) {
        try {
            String s = safeReadString(ctx, path, null);
            if (s == null || s.isBlank()) {
                return null;
            }
            return java.time.LocalDate.parse(s);
        } catch (Exception e) {
            return null;
        }
    }

    public static Boolean safeReadBoolean(DocContext ctx, String path, Boolean def) {
        try {
            Object v = ctx.read(path);
            if (v == null) {
                return def;
            }
            if (v instanceof Boolean) {
                return (Boolean) v;
            }
            String s = String.valueOf(v).trim();
            if (s.equalsIgnoreCase("true") || s.equalsIgnoreCase("y")
                || s.equalsIgnoreCase("yes")) {
                return Boolean.TRUE;
            }
            if (s.equalsIgnoreCase("false") || s.equalsIgnoreCase("n")
                || s.equalsIgnoreCase("no")) {
                return Boolean.FALSE;
            }
            return def;
        } catch (Exception e) {
            return def;
        }
    }

    public static java.math.BigDecimal safeReadBigDecimal(DocContext ctx, String path) {
        try {
            Object v = ctx.read(path);
            if (v == null) {
                return null;
            }
            if (v instanceof Number) {
                return new BigDecimal(String.valueOf(v));
            }
            return new BigDecimal(String.valueOf(v));
        } catch (Exception e) {
            return null;
        }
    }
}
