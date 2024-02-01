package uk.gov.hmcts.opal.service;

import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import static org.springframework.web.client.RestClient.ResponseSpec;
import static org.springframework.web.client.RestClient.RequestBodySpec;
import static org.springframework.web.client.RestClient.RequestBodyUriSpec;
import static org.springframework.web.client.RestClient.RequestHeadersSpec;
import static org.springframework.web.client.RestClient.RequestHeadersUriSpec;

public abstract class RestClientMockBase {

    @Mock
    protected RestClient restClient;

    @Mock
    protected RequestHeadersUriSpec requestHeaderUriSpec;

    @Mock
    protected RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    protected RequestHeadersSpec requestHeaderSpec;

    @Mock
    protected RequestBodySpec requestBodySpec;

    @Mock
    protected ResponseSpec responseSpec;


    @SuppressWarnings("unchecked")
    protected void mockRestClientGet() {
        when(restClient.get()).thenReturn(requestHeaderUriSpec);
        when(requestHeaderUriSpec.uri(anyString())).thenReturn(requestHeaderSpec);
        when(requestHeaderSpec.retrieve()).thenReturn(responseSpec);
    }

    @SuppressWarnings("unchecked")
    protected void mockRestClientPost() {
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any(MediaType.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
    }

}
