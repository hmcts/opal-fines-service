package uk.gov.hmcts.opal.util;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;

import java.io.StringReader;
import java.io.StringWriter;

public class XmlUtil {

    public <T> String marshalXmlString(T object, Class<T> clzz) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(clzz);
            Marshaller marshaller = jaxbContext.createMarshaller();

            // To format the XML output
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            StringWriter sw = new StringWriter();
            marshaller.marshal(object, sw);
            return sw.toString();
        } catch (JAXBException e) {
            throw new RuntimeException("Error serializing object to XML", e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T unmarshalXmlString(String xmlString, Class<T> clzz) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(clzz);
            return (T) jaxbContext.createUnmarshaller().unmarshal(new StringReader(xmlString));
        } catch (JAXBException e) {
            throw new RuntimeException("Error deserializing XML to object", e);
        }
    }
}
