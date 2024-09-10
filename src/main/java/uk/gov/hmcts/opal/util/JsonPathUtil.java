package uk.gov.hmcts.opal.util;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.PathNotFoundException;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "JsonPathUtil")
public class JsonPathUtil {

    public static DocContext createDocContext(String document) {
        Configuration config = Configuration.defaultConfiguration().addOptions(Option.DEFAULT_PATH_LEAF_TO_NULL);
        return new DocContext(JsonPath.parse(document, config), document);
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
    }
}
