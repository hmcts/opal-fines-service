package uk.gov.hmcts.opal.controllers.advice;

import lombok.AllArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;
import uk.gov.hmcts.opal.annotation.JsonSchemaValidated;
import uk.gov.hmcts.opal.exception.JsonSchemaValidationException;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

@ControllerAdvice
@AllArgsConstructor
public class JsonSchemaValidationAdvice extends RequestBodyAdviceAdapter {

    private final JsonSchemaValidationService jsonSchemaValidationService;

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return methodParameter.hasParameterAnnotation(JsonSchemaValidated.class);
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage,
                                           MethodParameter parameter,
                                           Type targetType,
                                           Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
        byte[] bodyBytes = inputMessage.getBody().readAllBytes();
        String body = new String(bodyBytes, StandardCharsets.UTF_8);

        JsonSchemaValidated annotation = parameter.getParameterAnnotation(JsonSchemaValidated.class);
        if (annotation != null) {
            String schemaPath = annotation.schemaPath();
            rejectTimelineDataForDraftAccountRequests(body, schemaPath);
            jsonSchemaValidationService.validateOrError(body, schemaPath);
        }

        return new HttpInputMessage() {
            @Override
            public HttpHeaders getHeaders() {
                return inputMessage.getHeaders();
            }

            @Override
            public InputStream getBody() {
                return new ByteArrayInputStream(bodyBytes);
            }
        };
    }

    private static void rejectTimelineDataForDraftAccountRequests(String body, String schemaPath) {
        if (schemaPath.contains("draft-account")
            && (schemaPath.contains("addDraftAccountRequest")
            || schemaPath.contains("replaceDraftAccountRequest")
            || schemaPath.contains("updateDraftAccountRequest"))
            && body.contains("\"timeline_data\"")) {
            throw new JsonSchemaValidationException("timeline_data is not allowed in draft account requests");
        }
    }
}
