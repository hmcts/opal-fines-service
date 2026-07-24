package uk.gov.hmcts.opal.service.hmrc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.RequestHeadersSpec;
import org.springframework.web.client.RestClient.RequestHeadersUriSpec;
import org.springframework.web.client.RestClient.ResponseSpec;
import uk.gov.hmcts.opal.service.hmrc.response.HMRCAuthToken;

@ExtendWith(MockitoExtension.class)
public class HmrcAuthServiceTest {

    @Mock
    private RestClient restClient;
    private String clientId = "TEST_CLIENT_ID";
    private String clientSecret = "TEST_CLIENT_SECRET";
    private String scope = "TEST_SCOPE_1+TEST_SCOPE_2";

    private String url = "https://test.com/auth";

    @Captor
    private ArgumentCaptor<URI> uriCaptor;

    private HmrcAuthService service;

    @BeforeEach
    void beforeEach() {
        service = new HmrcAuthService(restClient, clientId, clientSecret, scope, url);
    }

    @Test
    @SuppressWarnings("unchecked")
    void getAuthToken_buildsUrlCorrectly() {
        RequestHeadersUriSpec requestHeadersUriSpec = mock(RequestHeadersUriSpec.class);
        RequestHeadersSpec requestHeadersSpec = mock(RequestHeadersSpec.class);
        ResponseSpec responseSpec = mock(ResponseSpec.class);
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(URI.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(HMRCAuthToken.class)).thenReturn(mock(HMRCAuthToken.class));

        service.getAuthToken();

        verify(requestHeadersUriSpec).uri(uriCaptor.capture());
        URI uri = uriCaptor.getValue();
        String query = uri.getQuery();
        assertThat(uri.getScheme() + "://" + uri.getHost() + uri.getPath()).isEqualTo(url);
        assertThat(query).contains("client_id=" + clientId);
        assertThat(query).contains("client_secret=" + clientSecret);
        assertThat(query).contains("scope=" + scope);
        assertThat(query).contains("grant_type=client_credentials");
    }
}
