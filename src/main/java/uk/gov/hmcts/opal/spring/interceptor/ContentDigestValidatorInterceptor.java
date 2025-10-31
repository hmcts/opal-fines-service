package uk.gov.hmcts.opal.spring.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.gov.hmcts.opal.spring.exceptions.InvalidContentDigestException;
import uk.gov.hmcts.opal.spring.http.CachedBodyHttpServletRequest;
import uk.gov.hmcts.opal.spring.properties.ContentDigestProperties;

@Slf4j
@Component
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
    public boolean preHandle(HttpServletRequest request,
        HttpServletResponse response, Object handler) throws Exception {
        byte[] body = ((CachedBodyHttpServletRequest) request).getCachedBody();
        if (!enforce || body.length == 0) {
            return true;
        }

        String contentDigest = getAndValidateContentDigestHeader(request);
        List<Map.Entry<String, String>> entries = getAndValidateDigestEntries(contentDigest);

        List<String> tried = new ArrayList<>();
        boolean matched = false;

        for (var entry : entries) {
            String algo = entry.getKey();
            String expectedB64 = entry.getValue();

            if (!supportedAlgosLower.contains(algo)) {
                tried.add(algo + " (unsupported)");
                continue;
            }

            String jcaName = rfcToJca.get(algo);

            byte[] expectedBytes;
            try {
                expectedBytes = decodeSfBinary(expectedB64); // tolerant Base64 decode
            } catch (IllegalArgumentException e) {
                tried.add(algo + " (bad base64)");
                continue;
            }

            MessageDigest md;
            try {
                md = MessageDigest.getInstance(jcaName);
            } catch (NoSuchAlgorithmException e) {
                tried.add(algo + " (JCA unsupported)");
                continue;
            }

            byte[] actualBytes = md.digest(body);
            if (MessageDigest.isEqual(actualBytes, expectedBytes)) {
                matched = true;
                break;
            } else {
                tried.add(algo + " (mismatch)");
            }
        }

        if (!matched) {
            throw new InvalidContentDigestException("Digest validation failed",
                "Body hash did not match any supported digest algorithms. Supported algorithms ("
                    + String.join(",", supportedAlgosLower) + "). Found digests: "
                    + String.join(", ", tried));
        }

        log.debug("Content-Digest verification passed for algorithms: {}", tried);
        return true;
    }

    private List<Entry<String, String>> getAndValidateDigestEntries(String contentDigest) {
        // Split by commas (multiple digests)
        String[] parts = contentDigest.split("\\s*,\\s*");
        List<Map.Entry<String, String>> entries = new ArrayList<>();
        for (String part : parts) {
            Matcher matcher = ENTRY_PATTERN.matcher(part);
            if (matcher.matches()) {
                String algo = matcher.group(1).toLowerCase(Locale.ROOT);
                String b64 = matcher.group("b64");
                entries.add(Map.entry(algo, b64));
            }
        }

        if (entries.isEmpty()) {
            throw new InvalidContentDigestException("Invalid Content-Digest header",
                "No valid digest entries found in header: " + contentDigest);
        }
        return entries;
    }

    private String getAndValidateContentDigestHeader(HttpServletRequest request) {
        Enumeration<String> headers = request.getHeaders(CONTENT_DIGEST);
        String contentDigest;
        if (headers == null || !headers.hasMoreElements()) {
            contentDigest = null;
        } else {
            contentDigest = String.join(", ", Collections.list(headers));
        }
        if (contentDigest == null || contentDigest.isBlank()) {
            throw new InvalidContentDigestException(
                "Missing/Blank Content-Digest header",
                "The Content-Digest header must be provided with a non blank value.");
        }
        return contentDigest;
    }

    private static byte[] decodeSfBinary(String base64NoColons) {
        String b64 = base64NoColons;
        int mod = b64.length() % 4;
        if (mod != 0) {
            b64 = b64 + "===".substring(mod - 1); // append '=' padding if needed
        }
        return Base64.getDecoder().decode(b64);
    }
}