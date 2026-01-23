package uk.gov.hmcts.opal.config;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.lang.reflect.Field;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import uk.gov.hmcts.opal.interceptor.AcceptHeaderInterceptor;
import uk.gov.hmcts.opal.spring.interceptor.ContentDigestValidatorInterceptor;

@ExtendWith(MockitoExtension.class)
class WebConfigTest {

    @Mock
    private ContentDigestValidatorInterceptor contentDigestValidatorInterceptor;

    @Mock
    private AcceptHeaderInterceptor acceptHeaderInterceptor;

    @InjectMocks
    private WebConfig webConfig;

    @SneakyThrows
    private void updateEnforcerContentDigest(boolean enforce) {
        Field field = WebConfig.class.getDeclaredField("enforceContentDigest");
        field.setAccessible(true);
        field.set(webConfig, enforce);
    }

    @Nested
    @DisplayName("addInterceptors")
    class AddInterceptors {

        @Test
        @DisplayName("Should add accept header and content digest validator interceptor when enforced")
        void addInterceptors_shouldAddContentDigestValidatorInterceptorWhenEnforced() {
            InterceptorRegistry registry = mock(InterceptorRegistry.class);
            updateEnforcerContentDigest(true);
            webConfig.addInterceptors(registry);
            verify(registry).addInterceptor(acceptHeaderInterceptor);
            verify(registry).addInterceptor(contentDigestValidatorInterceptor);
            verifyNoMoreInteractions(registry);

        }

        @Test
        @DisplayName("Should add accept header but not add content digest validator interceptor when not enforced")
        void addInterceptors_shouldNotAddContentDigestValidatorInterceptorWhenNotEnforced() {
            InterceptorRegistry registry = mock(InterceptorRegistry.class);
            updateEnforcerContentDigest(false);
            webConfig.addInterceptors(registry);
            verify(registry).addInterceptor(acceptHeaderInterceptor);
            verifyNoMoreInteractions(registry);
        }
    }
}
