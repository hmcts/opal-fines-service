package uk.gov.hmcts.opal.util;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import org.xml.sax.SAXException;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

public class XmlUtil {

    public static final String XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
    public static final SchemaFactory SCHEMA_FACTORY = SchemaFactory.newInstance(XML_SCHEMA);

    private XmlUtil() {
        // Do not need to create an instance.
    }

    public static <T> String marshalXmlString(T object, Class<T> clzz) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(clzz);
        Marshaller marshaller = jaxbContext.createMarshaller();

        // To format the XML output
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        StringWriter sw = new StringWriter();
        marshaller.marshal(object, sw);
        return sw.toString();
    }

    @SuppressWarnings("unchecked")
    public static <T> T unmarshalXmlString(String xmlString, Class<T> clzz) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(clzz);
        return (T) jaxbContext.createUnmarshaller().unmarshal(new StringReader(xmlString));
    }

    public static void validateXmlString(String schemaDoc, String xmlString) {
        if (schemaDoc == null) {
            return;
        }
        try {
            Source xmlsource = new StreamSource(new StringReader(xmlString));
            Source schemaSource = new StreamSource(new StringReader(schemaDoc));
            // Will the schema file need to be read from the file system, or passed in as a String?
            // File schemaFile = new File(schemaDoc);
            // Schema schema = SCHEMA_FACTORY.newSchema(schemaFile);
            Schema schema = SCHEMA_FACTORY.newSchema(schemaSource);
            Validator validator = schema.newValidator();
            validator.validate(xmlsource);
        } catch (IOException | SAXException e) {
            throw new RuntimeException(e);
        }
    }
}
