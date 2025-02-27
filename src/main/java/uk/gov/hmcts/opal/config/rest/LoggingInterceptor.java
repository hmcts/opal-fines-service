package uk.gov.hmcts.opal.config.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.*;

import java.io.IOException;

@Slf4j
public class LoggingInterceptor implements ClientHttpRequestInterceptor {
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        log.info("Request: {} {}", request.getMethod(), request.getURI());
        log.info("Headers: {}", request.getHeaders());
        log.info("Body: {}", new String(body));

        ClientHttpResponse response = execution.execute(request, body);
        log.info("Response Status: {}", response.getStatusCode());
        log.info("Response Headers: {}", response.getHeaders());
        return response;
    }
}
