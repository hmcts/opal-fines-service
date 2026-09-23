package uk.gov.hmcts.opal.service.filehandler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import feign.RequestTemplate;
import feign.Target;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpHeaders;
import uk.gov.hmcts.opal.common.user.authentication.service.SystemUserAuthenticationService;
import uk.gov.hmcts.opal.common.user.authentication.service.SystemUserEnum;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.client.InterfaceFilesApiClient;
import uk.gov.hmcts.opal.interceptor.FileHandlerRequestInterceptor;

@ExtendWith(MockitoExtension.class)
class FileHandlerRequestInterceptorTest {

    @Mock
    private ObjectProvider<SystemUserAuthenticationService> authenticationServiceProvider;

    @Mock
    private SystemUserAuthenticationService authenticationService;

    private FileHandlerSystemUserContext systemUserContext;
    private FileHandlerRequestInterceptor interceptor;

    @BeforeEach
    void setUp() {
        systemUserContext = new FileHandlerSystemUserContext();
        interceptor = new FileHandlerRequestInterceptor(systemUserContext, authenticationServiceProvider);
    }

    @Test
    void apply_addsSystemUserBearerTokenToFileHandlerRequest() {
        RequestTemplate template = fileHandlerRequestTemplate();
        when(authenticationServiceProvider.getObject()).thenReturn(authenticationService);
        when(authenticationService.getSystemUserAuthenticationToken(SystemUserEnum.OPAL_SYSTEM_USER))
            .thenReturn("access-token");

        systemUserContext.executeAs(SystemUserEnum.OPAL_SYSTEM_USER, () -> {
            interceptor.apply(template);
            return null;
        });

        assertThat(template.headers().get(HttpHeaders.AUTHORIZATION)).containsExactly("Bearer access-token");
        verify(authenticationService).getSystemUserAuthenticationToken(SystemUserEnum.OPAL_SYSTEM_USER);
    }

    @Test
    void apply_withoutSelectedSystemUserFailsForFileHandlerRequest() {
        RequestTemplate template = fileHandlerRequestTemplate();

        assertThatThrownBy(() -> interceptor.apply(template))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("system user");
        verifyNoInteractions(authenticationServiceProvider);
    }

    @Test
    void apply_ignoresOtherFeignClients() {
        RequestTemplate template = new RequestTemplate();
        template.feignTarget(new Target.HardCodedTarget<>(Runnable.class, "other-client", "http://localhost"));

        interceptor.apply(template);

        assertThat(template.headers()).doesNotContainKey(HttpHeaders.AUTHORIZATION);
        verifyNoInteractions(authenticationServiceProvider);
    }

    private RequestTemplate fileHandlerRequestTemplate() {
        RequestTemplate template = new RequestTemplate();
        template.feignTarget(new Target.HardCodedTarget<>(
            InterfaceFilesApiClient.class,
            "interfaceFiles",
            "http://localhost"
        ));
        return template;
    }
}
