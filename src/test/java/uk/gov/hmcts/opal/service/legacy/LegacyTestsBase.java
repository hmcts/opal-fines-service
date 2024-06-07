package uk.gov.hmcts.opal.service.legacy;

import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.util.XmlUtil;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import static org.springframework.web.client.RestClient.ResponseSpec;
import static org.springframework.web.client.RestClient.RequestBodySpec;
import static org.springframework.web.client.RestClient.RequestBodyUriSpec;
import static org.springframework.web.client.RestClient.RequestHeadersSpec;
import static org.springframework.web.client.RestClient.RequestHeadersUriSpec;

public abstract class LegacyTestsBase {

    public static final String NOT_YET_IMPLEMENTED = "Not Yet Implemented";

    @Mock
    RestClient restClient;

    @Mock
    LegacyGatewayProperties properties;

    @Mock
    RequestHeadersUriSpec requestHeaderUriSpec;

    @Mock
    RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    RequestHeadersSpec requestHeaderSpec;

    @Mock
    RequestBodySpec requestBodySpec;

    @Mock
    ResponseSpec responseSpec;

    XmlUtil xmlUtil = new XmlUtil();


    @SuppressWarnings("unchecked")
    void mockRestClientGet() {
        when(restClient.get()).thenReturn(requestHeaderUriSpec);
        when(requestHeaderUriSpec.uri(anyString())).thenReturn(requestHeaderSpec);
        when(requestHeaderSpec.retrieve()).thenReturn(responseSpec);
    }

    @SuppressWarnings("unchecked")
    void mockRestClientPost() {
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any(MediaType.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
    }

    <T> String marshalXmlString(T object, Class<T> clzz) {
        return xmlUtil.marshalXmlString(object, clzz);
    }
}
