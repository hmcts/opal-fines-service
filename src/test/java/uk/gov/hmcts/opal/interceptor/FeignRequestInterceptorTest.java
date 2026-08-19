package uk.gov.hmcts.opal.interceptor;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import feign.RequestTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.service.hmrc.HmrcAuthentication;

@ExtendWith(MockitoExtension.class)
class FeignRequestInterceptorTest {


    @Mock
    private HmrcAuthentication auth;

    @Mock
    private RequestTemplate request;

    @InjectMocks
    private FeignRequestInterceptor interceptor;

    @Test
    void appliesAuthenticationTokenWhenAuthIsNotNull() {
        when(request.url()).thenReturn("/some/other/url");
        when(auth.getToken()).thenReturn("some-token");

        interceptor.apply(request);

        verify(request).header("Authorization", "Bearer some-token");
    }

    @Test
    void doesNotApplyWhenUrlIsOAuth() {
        when(request.url()).thenReturn("/oauth/token");
        interceptor.apply(request);

        verify(request).url();
        verifyNoMoreInteractions(request);
    }
}
