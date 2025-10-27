package uk.gov.hmcts.opal.spring.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Locale;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import uk.gov.hmcts.opal.spring.http.CapturingResponseWrapper;
import uk.gov.hmcts.opal.spring.properties.ContentDigestProperties;

@Slf4j
@Component
public class ContentDigestResponseFilter extends OncePerRequestFilter {

    private final ContentDigestProperties props;

    public ContentDigestResponseFilter(ContentDigestProperties props) {
        this.props = props;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
        HttpServletResponse response,
        FilterChain chain)
        throws ServletException, IOException {

        var cfg = props.getResponse();
        if (!Boolean.TRUE.equals(cfg.getAutoGenerate())) {
            chain.doFilter(request, response);
            return;
        }

        // Wrap response to capture body bytes
        CapturingResponseWrapper wrapped = new CapturingResponseWrapper(response);

        // Let MVC write the response into our buffer
        chain.doFilter(request, wrapped);

        // Grab the bytes that would be sent
        byte[] body = wrapped.getCaptured();

        // If there is no body, just commit an empty response (no header)
        if (body.length == 0) {
            response.setContentLength(0);
            response.flushBuffer();
            return;
        }

        // Compute digest over the exact bytes
        String jcaAlgo = cfg.getAlgorithm();              // e.g. "SHA-256"
        String rfcToken = jcaAlgo.toLowerCase(Locale.ROOT).replace('_', '-'); // "sha-256"
        try {
            MessageDigest md = MessageDigest.getInstance(jcaAlgo);
            byte[] digest = md.digest(body);
            String sfBinary = ":" + Base64.getEncoder().encodeToString(digest) + ":";

            // Add header BEFORE sending body
            response.setHeader("Content-Digest", rfcToken + "=" + sfBinary);
            log.debug("Added Content-Digest: {}={}", rfcToken, sfBinary);
        } catch (Exception ex) {
            // Policy choice: if hashing fails, send body without the header
            log.warn("Failed to compute Content-Digest with {}", jcaAlgo, ex);
        }

        // Write the body to the real response and set Content-Length
        if (body.length <= Integer.MAX_VALUE) {
            response.setContentLength(body.length);
        }
        response.getOutputStream().write(body);
    }
}