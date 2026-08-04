package uk.gov.hmcts.opal.interceptor;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import feign.RequestTemplate;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.service.hmrc.HmrcAuthentication;

@ExtendWith(MockitoExtension.class)
class FeignRequestInterceptorTest {

    @Nested
    class AuthExists {

        @Mock
        private HmrcAuthentication auth;

        @Mock
        private RequestTemplate request;

        @InjectMocks
        private FeignRequestInterceptor interceptor;

        @Test
        void appliesAuthenticationTokenWhenAuthIsNotNull() {
            when(auth.getToken()).thenReturn("some-token");

            interceptor.apply(request);

            verify(request).header("Authorization", "Bearer some-token");
        }
    }

    @Nested
    class AuthNull {

        @Mock
        private RequestTemplate request;

        @InjectMocks
        private FeignRequestInterceptor interceptor;

        @Test
        void doesNotApplyAuthTokenWhenNull() {
            interceptor.apply(request);

            verifyNoInteractions(request);
        }
    }
}
