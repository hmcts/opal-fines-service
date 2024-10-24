package uk.gov.hmcts.opal.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.method.HandlerMethod;
import uk.gov.hmcts.opal.annotation.CheckAcceptHeader;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AcceptHeaderInterceptorTest {

    private AcceptHeaderInterceptor interceptor;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HandlerMethod handlerMethod;

    @BeforeEach
    void setUp() {
        interceptor = new AcceptHeaderInterceptor();
    }

    @Test
    void preHandle_WithCheckAcceptHeaderAnnotationAndValidJsonAcceptHeader_ShouldReturnTrue() throws Exception {
        when(handlerMethod.hasMethodAnnotation(CheckAcceptHeader.class)).thenReturn(true);
        when(request.getHeader("Accept")).thenReturn("application/json");

        boolean result = interceptor.preHandle(request, response, handlerMethod);

        assertTrue(result);
        verifyNoInteractions(response);
    }

    @Test
    void preHandle_WithCheckAcceptHeaderAnnotationAndValidWildcardAcceptHeader_ShouldReturnTrue() throws Exception {
        when(handlerMethod.hasMethodAnnotation(CheckAcceptHeader.class)).thenReturn(true);
        when(request.getHeader("Accept")).thenReturn("*/*");

        boolean result = interceptor.preHandle(request, response, handlerMethod);

        assertTrue(result);
        verifyNoInteractions(response);
    }

    @Test
    void preHandle_WithCheckAcceptHeaderAnnotationAndInvalidAcceptHeader_ShouldReturnFalse() throws Exception {
        when(handlerMethod.hasMethodAnnotation(CheckAcceptHeader.class)).thenReturn(true);
        when(request.getHeader("Accept")).thenReturn("application/xml");

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        boolean result = interceptor.preHandle(request, response, handlerMethod);

        assertFalse(result);
        verify(response).setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
        writer.flush();
        assertTrue(stringWriter.toString().contains("\"error\":\"Not Acceptable\""));
        assertTrue(stringWriter.toString().contains("\"message\":\"The requested media type is not supported\""));
    }


    @Test
    void preHandle_WithoutCheckAcceptHeaderAnnotation_ShouldReturnTrue() throws Exception {
        when(handlerMethod.hasMethodAnnotation(CheckAcceptHeader.class)).thenReturn(false);

        boolean result = interceptor.preHandle(request, response, handlerMethod);

        assertTrue(result);
        verifyNoInteractions(response);
    }
}
