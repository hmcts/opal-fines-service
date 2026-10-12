package uk.gov.hmcts.opal.dto.legacy.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.xml.bind.JAXBException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.opal.util.XmlUtil;

class CreditorAccountTypeReferenceTest {

    @ParameterizedTest
    @ValueSource(strings = {"creditor_account_type", "account_type"})
    void whenXmlContainsSupportedAccountTypeName_populatesField_happyPath(String fieldName) throws JAXBException {
        CreditorAccountTypeReference reference = XmlUtil.unmarshalXmlString("""
            <creditorAccountTypeReference>
                <%1$s>MN</%1$s>
            </creditorAccountTypeReference>
            """.formatted(fieldName), CreditorAccountTypeReference.class);

        assertEquals("MN", reference.getCreditorAccountType());
    }
}
