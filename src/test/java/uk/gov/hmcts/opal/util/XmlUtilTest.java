package uk.gov.hmcts.opal.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class XmlUtilTest {

    @Test
    void testSchemaValidation_emptySchema() {
        assertThrows(
            RuntimeException.class,
            () -> XmlUtil.validateXmlString("", getBasicXmlDoc())
        );
    }

    @Test
    void testSchemaValidation_emptyXmlDoc() {
        assertThrows(
            RuntimeException.class,
            () -> XmlUtil.validateXmlString(getBasicSchema(), "")
        );
    }

    @Test
    void testSchemaValidation_success() {
        XmlUtil.validateXmlString(getBasicSchema(), getBasicXmlDoc());
    }

    private String getBasicSchema() {
        return """
            <?xml version="1.0" encoding="utf-8"?>
            <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">
              <xs:element name="root"/>
            </xs:schema>
            """;
    }

    private String getBasicXmlDoc() {
        return """
            <?xml version="1.0" encoding="utf-8"?>
            <root>Anything in here</root>
            """;
    }
}
