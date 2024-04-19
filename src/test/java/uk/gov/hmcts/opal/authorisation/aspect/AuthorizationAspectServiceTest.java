package uk.gov.hmcts.opal.authorisation.aspect;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = AuthorizationAspectService.class)
@ExtendWith(MockitoExtension.class)
class AuthorizationAspectServiceTest {

    @MockBean
    private HttpServletRequest servletRequest;

    @Autowired
    private AuthorizationAspectService authorizationAspectService;

    @Test
    void getRequestHeaderValue_WhenNoStringArgumentExists_ReturnsNull() {
        Object[] args = {123, true, new Object()};

        String headerValue = authorizationAspectService.getRequestHeaderValue(args);

        assertEquals(null, headerValue);
    }

    @Test
    void getAuthorization_WhenAuthHeaderValueNotNull_ReturnsOptionalWithValue() {
        String authHeaderValue = "Bearer token";

        Optional<String> result = authorizationAspectService.getAuthorization(authHeaderValue);

        assertEquals(Optional.of(authHeaderValue), result);
    }

    @Test
    void getAuthorization_WhenRequestAttributesNotNull_ReturnsOptionalWithValue() {
        String authHeaderValue = "Bearer token";
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(servletRequest));
        when(servletRequest.getHeader(AuthorizationAspectService.AUTHORIZATION)).thenReturn(authHeaderValue);

        Optional<String> result = authorizationAspectService.getAuthorization(null);

        assertEquals(Optional.of(authHeaderValue), result);
    }

    @Test
    void getAuthorization_WhenRequestAttributesNull_ReturnsOptionalEmpty() {
        RequestContextHolder.setRequestAttributes(null);

        Optional<String> result = authorizationAspectService.getAuthorization(null);

        assertEquals(Optional.empty(), result);
    }
}
