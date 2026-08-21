package uk.gov.hmcts.opal.controllers.advice;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import uk.gov.hmcts.opal.annotation.JsonSchemaValidated;
import uk.gov.hmcts.opal.exception.ProhibitedDraftAccountRequestFieldException;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;

class JsonSchemaValidationAdviceTest {

    private static final String DRAFT_ACCOUNT_SCHEMA = "opal/draft-account/addDraftAccountRequest.json";

    private final JsonSchemaValidationService jsonSchemaValidationService = mock(JsonSchemaValidationService.class);
    private final JsonSchemaValidationAdvice advice = new JsonSchemaValidationAdvice(jsonSchemaValidationService);

    @Test
    void beforeBodyRead_rejectsTopLevelTokenDerivedField() {
        assertThrows(
            ProhibitedDraftAccountRequestFieldException.class,
            () -> advice.beforeBodyRead(inputMessage("""
                {
                  "business_unit_id": 78,
                  "submitted_by": "client-user",
                  "account": {}
                }"""), methodParameter(), targetType(), converterType())
        );
    }

    @Test
    void beforeBodyRead_allowsTokenDerivedFieldNameInsideNestedValue() {
        String body = """
            {
              "business_unit_id": 78,
              "account": {
                "note": "submitted_by"
              }
            }""";

        assertDoesNotThrow(() -> advice.beforeBodyRead(
            inputMessage(body),
            methodParameter(),
            targetType(),
            converterType()
        ));

        verify(jsonSchemaValidationService).validateOrError(body, DRAFT_ACCOUNT_SCHEMA);
    }

    private MethodParameter methodParameter() {
        JsonSchemaValidated annotation = mock(JsonSchemaValidated.class);
        when(annotation.schemaPath()).thenReturn(DRAFT_ACCOUNT_SCHEMA);

        MethodParameter methodParameter = mock(MethodParameter.class);
        when(methodParameter.getParameterAnnotation(JsonSchemaValidated.class)).thenReturn(annotation);
        return methodParameter;
    }

    private HttpInputMessage inputMessage(String body) {
        return new HttpInputMessage() {
            @Override
            public InputStream getBody() {
                return new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));
            }

            @Override
            public HttpHeaders getHeaders() {
                return HttpHeaders.EMPTY;
            }
        };
    }

    private Type targetType() {
        return Object.class;
    }

    @SuppressWarnings("unchecked")
    private Class<? extends HttpMessageConverter<?>> converterType() {
        return (Class<? extends HttpMessageConverter<?>>) (Class<?>) HttpMessageConverter.class;
    }
}
