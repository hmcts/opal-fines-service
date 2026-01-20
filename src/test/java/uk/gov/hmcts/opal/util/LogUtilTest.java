package uk.gov.hmcts.opal.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

class LogUtilTest {

    @AfterEach
    void tearDown() {
        // Ensure any SecurityContext set during tests is cleared
        SecurityContextHolder.clearContext();
    }

    @Test
    void getIpAddress_noAuthentication_returnsNull() {
        // Ensure no SecurityContext / no Authentication present
        SecurityContextHolder.clearContext();

        String ip = LogUtil.getIpAddress();
        assertNull(ip, "Expected null when there is no Authentication in SecurityContext");
    }

    @Test
    void getIpAddress_nonWebDetails_returnsNull() {
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
    void getCurrentDateTime_withFixedClock_returnsExpectedOffsetDateTime() {
        // set a fixed instant and create a fixed Clock
        Instant fixedInstant = Instant.parse("2025-12-18T12:34:56Z");
        Clock fixedClock = Clock.fixed(fixedInstant, ZoneOffset.UTC);

        OffsetDateTime expected = OffsetDateTime.ofInstant(fixedInstant, ZoneOffset.UTC);
        OffsetDateTime actual = OffsetDateTime.now(fixedClock);

        assertEquals(expected, actual,
            "getCurrentDateTime should return an OffsetDateTime derived from the provided clock");
    }
}