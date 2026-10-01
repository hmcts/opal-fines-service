package uk.gov.hmcts.opal.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.common.user.authentication.service.SystemUserAuthenticationService;
import uk.gov.hmcts.opal.service.filehandler.FileHandlerSystemUserContext;
import uk.gov.hmcts.opal.service.filehandler.clients.FileHandlerClient;

@Component
@AllArgsConstructor
public class FileHandlerRequestInterceptor implements RequestInterceptor {

    private final FileHandlerSystemUserContext systemUserContext;
    private final ObjectProvider<SystemUserAuthenticationService> authenticationServiceProvider;

    @Override
    public void apply(RequestTemplate template) {
        if (template.feignTarget() == null
            || !FileHandlerClient.class.equals(template.feignTarget().type())) {
            return;
        }

        var systemUser = systemUserContext.getRequiredSystemUser();
        var token = authenticationServiceProvider.getObject().getSystemUserAuthenticationToken(systemUser);

        template.header(HttpHeaders.AUTHORIZATION, "Bearer "  + token);
    }

}
