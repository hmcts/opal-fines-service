package uk.gov.hmcts.opal.service.filehandler.clients;

import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.openfeign.support.JsonFormWriter;
import org.springframework.context.annotation.Bean;
import uk.gov.hmcts.opal.common.user.authentication.service.SystemUserAuthenticationService;
import uk.gov.hmcts.opal.interceptor.FileHandlerRequestInterceptor;
import uk.gov.hmcts.opal.service.filehandler.FileHandlerSystemUserContext;

public class FileHandlerFeignClientConfiguration {

    @Bean
    public ErrorDecoder fileHandlerErrorDecoder() {
        return new ErrorDecoder.Default();
    }

    @Bean
    public JsonFormWriter jsonFormWriter() {
        return new JsonFormWriter();
    }

    @Bean
    public RequestInterceptor requestInterceptor(
        FileHandlerSystemUserContext systemUserContext,
        ObjectProvider<SystemUserAuthenticationService> authenticationServiceProvider
    ) {
        return new FileHandlerRequestInterceptor(systemUserContext, authenticationServiceProvider);
    }

}
