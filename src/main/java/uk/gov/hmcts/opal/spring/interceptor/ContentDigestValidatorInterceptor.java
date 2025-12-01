package uk.gov.hmcts.opal.spring.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.gov.hmcts.common.exceptions.standard.InvalidContentDigestException;
import uk.gov.hmcts.opal.spring.http.CachedBodyHttpServletRequest;
import uk.gov.hmcts.opal.spring.properties.ContentDigestProperties;

@Slf4j
@Component
@Getter
public class ContentDigestValidatorInterceptor implements HandlerInterceptor {

    private static final String CONTENT_DIGEST = "Content-Digest";

    private static final Pattern ENTRY_PATTERN =
        Pattern.compile("(?i)\\s*([a-z0-9-]+)\\s*=\\s*:(?<b64>[A-Za-z0-9+/=]+):\\s*");

    private final boolean enforce;
    private final Map<String, String> rfcToJca;    // RFC token -> JCA name
    private final Set<String> supportedAlgosLower; // RFC tokens (lowercase)


    public ContentDigestValidatorInterceptor(ContentDigestProperties contentDigestProperties) {
        this.enforce = contentDigestProperties.getRequest().isEnforced();
        this.rfcToJca = contentDigestProperties.getRequest().getSupportedAlgorithms().entrySet().stream()
            .collect(java.util.stream.Collectors.toUnmodifiableMap(
                e -> e.getKey().toLowerCase(Locale.ROOT),
                Map.Entry::getValue
            ));
        this.supportedAlgosLower = java.util.Collections.unmodifiableSet(rfcToJca.keySet());
        log.info("Content-Digest enforce={}, algorithms={}", contentDigestProperties.getRequest().isEnforced(),
            rfcToJca);
    }


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
        throws Exception {
        byte[] body = ((CachedBodyHttpServletRequest) request).getCachedBody();
        if (!enforce || body.length == 0) {
            return true;
        }

        ContentDigest contentDigest = new ContentDigest(getAndValidateContentDigestHeader(request));

        byte[] expectedBytes = contentDigest.decodeSfBinary();
        MessageDigest md = contentDigest.getMessageDigest();

        byte[] actualBytes = md.digest(body);
        if (!MessageDigest.isEqual(actualBytes, expectedBytes)) {
            if (log.isDebugEnabled()) {
                log.debug("Content-Digest verification failed for algorithms: {}. Expected: {}, Actual: {}",
                    contentDigest.getAlgorithm(),
                    Base64.getEncoder().encodeToString(expectedBytes),
                    Base64.getEncoder().encodeToString(actualBytes));
            }
            throw new InvalidContentDigestException("Digest validation failed",
                "Body hash did not match for algorithm: " + contentDigest.getAlgorithm());
        }
        log.debug("Content-Digest verification passed for algorithms: {}", contentDigest.getAlgorithm());
        return true;
    }


    String getAndValidateContentDigestHeader(HttpServletRequest request) {
        String contentDigest = request.getHeader(CONTENT_DIGEST);
        if (contentDigest == null || contentDigest.isBlank()) {
            throw new InvalidContentDigestException(
                "Missing/Blank Content-Digest header",
                "The Content-Digest header must be provided with a non blank value.");
        }
        return contentDigest;
    }


    @RequiredArgsConstructor
    @Getter
    public class ContentDigest {

        private final String algorithm;
        private final String base64;

        public ContentDigest(String contentDigest) {
            if (contentDigest.contains(",")) {
                throw new InvalidContentDigestException("Invalid Content-Digest header",
                    "Multiple digest entries are not supported");
            }
            Matcher matcher = ENTRY_PATTERN.matcher(contentDigest);
            if (!matcher.matches()) {
                throw new InvalidContentDigestException("Invalid Content-Digest header",
                    "No valid digest entries found in header");
            }
            this.algorithm = matcher.group(1).toLowerCase(Locale.ROOT);
            this.base64 = matcher.group("b64");
            validate();
        }

        void validate() {
            if (getAlgorithm() == null || getAlgorithm().isBlank()) {
                throw new InvalidContentDigestException("Digest validation failed",
                    "Digest algorithm is missing or blank.");
            }
            if (!supportedAlgosLower.contains(getAlgorithm())) {
                throw new InvalidContentDigestException("Digest validation failed",
                    "Unsupported digest algorithm: " + getAlgorithm()
                        + ". Supported algorithms (" + String.join(",", supportedAlgosLower) + ").");
            }
        }

        public MessageDigest getMessageDigest() {
            try {
                return MessageDigest.getInstance(rfcToJca.get(getAlgorithm()));
            } catch (Exception e) {
                throw new InvalidContentDigestException("Digest validation failed",
                    "Unsupported digest algorithm: " + getAlgorithm()
                        + ". Supported algorithms (" + String.join(",", supportedAlgosLower) + ").");
            }
        }

        byte[] decodeSfBinary() {
            try {
                String b64 = getBase64();
                int mod = b64.length() % 4;
                if (mod != 0) {
                    b64 = b64 + "===".substring(mod - 1); // append '=' padding if needed
                }
                return Base64.getDecoder().decode(b64);
            } catch (Exception e) {
                throw new InvalidContentDigestException("Digest validation failed",
                    "Bad base64 encoding for algorithm: " + getAlgorithm());
            }
        }
    }
}