package uk.gov.hmcts.opal.service.hmrc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.service.hmrc.clients.HmrcAuthClient;
import uk.gov.hmcts.opal.service.hmrc.clients.HmrcAuthCreds;
import uk.gov.hmcts.opal.service.hmrc.clients.response.HmrcAuthToken;

@ExtendWith(MockitoExtension.class)
public class HmrcAuthServiceTest {

    @Mock
    private HmrcAuthClient authClient;
    @Mock
    private HmrcAuthCreds creds;

    @InjectMocks
    private HmrcAuthService service;


    @Test
    void getToken_orchesratesCallToAuthClientCorrectly() {
        String token = "Bearer XXXX";
        HmrcAuthToken response = new HmrcAuthToken(token, "type", 1000, "scope");
        when(authClient.getAuthToken(creds)).thenReturn(response);

        String returnedToken = service.getToken();

        assertThat(returnedToken).isEqualTo(token);
        verify(authClient).getAuthToken(creds);
    }
}
