package uk.gov.hmcts.opal.dto;

import jakarta.xml.bind.JAXBException;
import uk.gov.hmcts.opal.util.XmlUtil;

public interface ToXmlString {

    @SuppressWarnings("unchecked")
    default String toXmlString() throws JAXBException {
        Class clzz = this.getClass();
        return XmlUtil.marshalXmlString(this, clzz);
    }

    default String toXml() {
        try {
            return toXmlString();
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

}
