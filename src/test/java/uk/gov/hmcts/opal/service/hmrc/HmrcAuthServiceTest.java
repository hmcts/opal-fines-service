package uk.gov.hmcts.opal.service.hmrc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.RequestBodySpec;
import org.springframework.web.client.RestClient.RequestBodyUriSpec;
import org.springframework.web.client.RestClient.ResponseSpec;
import uk.gov.hmcts.opal.service.hmrc.response.HmrcAuthToken;

@ExtendWith(MockitoExtension.class)
public class HmrcAuthServiceTest {

    @Mock
    private RestClient restClient;
    private String clientId = "TEST_CLIENT_ID";
    private String clientSecret = "TEST_CLIENT_SECRET";
    private String scope = "TEST_SCOPE_1+TEST_SCOPE_2";
    private String grantType = "client_credentials";
    private HmrcAuthCreds creds = new HmrcAuthCreds(clientId, clientSecret, scope, grantType);
    private String url = "https://test.com/auth";

    @Captor
    private ArgumentCaptor<String> uriCaptor;
    @Captor
    private ArgumentCaptor<HmrcAuthCreds> hmrcAuthCredsCaptor;

    private HmrcAuthService service;

    @BeforeEach
    void beforeEach() {
        service = new HmrcAuthService(restClient, creds, url);
    }

    @Test
    void getAuthToken_callsHmrcEndpointCorrectly() {
        RequestBodyUriSpec requestBodyUriSpec = mock(RequestBodyUriSpec.class);
        RequestBodySpec requestBodySpec = mock(RequestBodySpec.class);
        RequestBodySpec requestBodySpec2 = mock(RequestBodySpec.class);
        ResponseSpec responseSpec = mock(ResponseSpec.class);
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(url)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(creds)).thenReturn(requestBodySpec2);
        when(requestBodySpec2.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(HmrcAuthToken.class)).thenReturn(mock(HmrcAuthToken.class));

        service.getAuthToken();

        verify(requestBodyUriSpec).uri(uriCaptor.capture());
        assertThat(uriCaptor.getValue()).isEqualTo(url);

        verify(requestBodySpec).body(hmrcAuthCredsCaptor.capture());
        HmrcAuthCreds creds = hmrcAuthCredsCaptor.getValue();
        assertThat(creds.getClientId()).isEqualTo(clientId);
        assertThat(creds.getClientSecret()).isEqualTo(clientSecret);
        assertThat(creds.getScope()).isEqualTo(scope);
        assertThat(creds.getGrantType()).isEqualTo(grantType);
    }
}
