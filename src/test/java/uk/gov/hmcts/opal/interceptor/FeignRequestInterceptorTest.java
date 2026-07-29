package uk.gov.hmcts.opal.interceptor;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.method.HandlerMethod;
import uk.gov.hmcts.opal.annotation.CheckAcceptHeader;
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
        void AppliesAuthenticationTokenWhenAuthIsAvailable() {
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
        void AppliesAuthenticationTokenWhenAuthIsAvailable() {
            interceptor.apply(request);

            verifyNoInteractions(request);
        }
    }
}
