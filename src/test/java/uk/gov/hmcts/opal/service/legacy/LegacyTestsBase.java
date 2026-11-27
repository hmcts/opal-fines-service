package uk.gov.hmcts.opal.service.legacy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import jakarta.xml.bind.JAXBException;
import org.mockito.Mock;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.common.legacy.client.LegacyGatewayClient;
import uk.gov.hmcts.opal.common.legacy.config.LegacyGatewayProperties;
import uk.gov.hmcts.opal.common.legacy.service.GatewayService;
import uk.gov.hmcts.opal.common.legacy.service.LegacyGatewayService;
import uk.gov.hmcts.opal.util.XmlUtil;

public abstract class LegacyTestsBase {

    public static final String NOT_YET_IMPLEMENTED = "Not Yet Implemented";

    @Mock
    protected LegacyGatewayProperties gatewayProperties;

    @Mock
    protected LegacyGatewayClient gatewayClient;

    protected GatewayService createGatewayService() {
        return new LegacyGatewayService(gatewayProperties, gatewayClient);
    }

    protected void mockGatewayResponse(String actionType, ResponseEntity<String> response) {
        when(gatewayClient.post(eq(actionType), anyString(), any())).thenReturn(response);
    }

    <T> String marshalXmlString(T object, Class<T> clzz) throws JAXBException {
        return XmlUtil.marshalXmlString(object, clzz);
    }
}
