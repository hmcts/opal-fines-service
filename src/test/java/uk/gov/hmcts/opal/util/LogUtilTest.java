package uk.gov.hmcts.opal.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.slf4j.MDC;

class LogUtilTest {

    @AfterEach
    void tearDown() {
        // Ensure any SecurityContext / RequestContext / MDC set during tests is cleared
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
        MDC.clear();
    }

    @Test
    void getIpAddress_noAuthentication_returnsNull() {
        // Ensure there is a RequestContext but no Authentication present so method proceeds past header check
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-User-IP")).thenReturn(null);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // Ensure no SecurityContext / no Authentication present
        SecurityContextHolder.clearContext();

        String ip = LogUtil.getIpAddress();
        assertNull(ip, "Expected null when there is no Authentication in SecurityContext");
    }

    @Test
    void getIpAddress_nonWebDetails_returnsNull() {
        // Provide a RequestAttributes with no header so method continues to check SecurityContext
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-User-IP")).thenReturn(null);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // Mock SecurityContext + Authentication with non-WebAuthenticationDetails
        Authentication auth = mock(Authentication.class);
        when(auth.getDetails()).thenReturn(new Object());

        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);

        String ip = LogUtil.getIpAddress();
        assertNull(ip, "Expected null when Authentication.getDetails() is not WebAuthenticationDetails");
    }

    @Test
    void getIpAddress_webAuthDetails_returnsRemoteAddress() {
        // Provide a RequestAttributes with no header so method continues to check SecurityContext
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-User-IP")).thenReturn(null);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // Mock SecurityContext + Authentication with WebAuthenticationDetails
        Authentication auth = mock(Authentication.class);
        WebAuthenticationDetails details = mock(WebAuthenticationDetails.class);
        when(details.getRemoteAddress()).thenReturn("203.0.113.45");
        when(auth.getDetails()).thenReturn(details);

        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);

        String ip = LogUtil.getIpAddress();
        assertEquals("203.0.113.45", ip, "Expected remote address returned from WebAuthenticationDetails");
    }

    @Test
    void getIpAddress_fromHttpHeader_returnsHeaderValue() {
        // Mock HttpServletRequest with X-User-IP header set
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-User-IP")).thenReturn("198.51.100.23");

        ServletRequestAttributes attrs = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attrs);

        String ip = LogUtil.getIpAddress();
        assertEquals("198.51.100.23", ip, "Expected IP from X-User-IP header");
    }

    @Test
    void getIpAddress_headerTakesPrecedenceOverAuthentication() {
        // Header present
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-User-IP")).thenReturn("198.51.100.23");
        ServletRequestAttributes attrs = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attrs);

        // Also set SecurityContext with a different remote address
        Authentication auth = mock(Authentication.class);
        WebAuthenticationDetails details = mock(WebAuthenticationDetails.class);
        when(details.getRemoteAddress()).thenReturn("203.0.113.45");
        when(auth.getDetails()).thenReturn(details);

        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);

        String ip = LogUtil.getIpAddress();
        // header should win
        assertEquals("198.51.100.23", ip, "Expected header IP to take precedence over authentication details");
    }

    @Test
    void getCurrentDateTime_withFixedClock_returnsExpectedOffsetDateTime() {
        // set a fixed instant and create a fixed Clock
        Instant fixedInstant = Instant.parse("2025-12-18T12:34:56Z");
        Clock fixedClock = Clock.fixed(fixedInstant, ZoneOffset.UTC);

        OffsetDateTime expected = OffsetDateTime.ofInstant(fixedInstant, ZoneOffset.UTC);
        OffsetDateTime actual = LogUtil.getCurrentDateTime(fixedClock);

        assertEquals(expected, actual,
            "getCurrentDateTime should return an OffsetDateTime derived from the provided clock");
    }

    @Test
    void createOpalOperation_setsMdcWhenNoOperationContext() {
        // Ensure there is no OperationContext available in the environment (test env typically has none).
        // createOpalOperation should return an id and set it in the MDC under "opal-operation-id".
        String operationId = LogUtil.createOpalOperation();

        String mdcValue = MDC.get("opal-operation-id");
        assertEquals(operationId, mdcValue, "createOpalOperation should set opal-operation-id in "
            + "MDC when no operation context exists");

        // cleanup - done in tearDown
    }
}
