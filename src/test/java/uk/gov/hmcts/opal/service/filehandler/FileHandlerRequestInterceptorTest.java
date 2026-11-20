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
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import uk.gov.hmcts.opal.common.user.authentication.service.SystemUserAuthenticationService;
import uk.gov.hmcts.opal.common.user.authentication.service.SystemUserEnum;
import uk.gov.hmcts.opal.interceptor.FileHandlerRequestInterceptor;
import uk.gov.hmcts.opal.service.filehandler.clients.FileHandlerClient;
import uk.gov.hmcts.opal.service.filehandler.clients.FileHandlerFeignClientConfiguration;

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
    void configuration_registersOnlyOneFileHandlerInterceptor() {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.registerBean(ObjectMapper.class, () -> JsonMapper.builder().build());
            context.registerBean(FileHandlerSystemUserContext.class);
            context.registerBean(SystemUserAuthenticationService.class, () -> authenticationService);
            context.register(FileHandlerFeignClientConfiguration.class);
            context.scan(FileHandlerRequestInterceptor.class.getPackageName());
            context.refresh();

            assertThat(context.getBeansOfType(FileHandlerRequestInterceptor.class)).hasSize(1);
        }
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
    void apply_replacesExistingAuthorizationHeader() {
        RequestTemplate template = fileHandlerRequestTemplate();
        template.header(HttpHeaders.AUTHORIZATION, "Bearer existing-token");
        when(authenticationServiceProvider.getObject()).thenReturn(authenticationService);
        when(authenticationService.getSystemUserAuthenticationToken(SystemUserEnum.OPAL_SYSTEM_USER))
            .thenReturn("system-user-token");

        systemUserContext.executeAs(SystemUserEnum.OPAL_SYSTEM_USER, () -> {
            interceptor.apply(template);
            return null;
        });

        assertThat(template.headers().get(HttpHeaders.AUTHORIZATION)).containsExactly("Bearer system-user-token");
    }

    @Test
    void apply_repeatedCallsKeepOnlyTheLatestAuthorizationHeader() {
        RequestTemplate template = fileHandlerRequestTemplate();
        when(authenticationServiceProvider.getObject()).thenReturn(authenticationService);
        when(authenticationService.getSystemUserAuthenticationToken(SystemUserEnum.OPAL_SYSTEM_USER))
            .thenReturn("first-token", "second-token");

        systemUserContext.executeAs(SystemUserEnum.OPAL_SYSTEM_USER, () -> {
            interceptor.apply(template);
            interceptor.apply(template);
            return null;
        });

        assertThat(template.headers().get(HttpHeaders.AUTHORIZATION)).containsExactly("Bearer second-token");
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
            FileHandlerClient.class,
            "interfaceFiles",
            "http://localhost"
        ));
        return template;
    }
}
