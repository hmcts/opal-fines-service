package uk.gov.hmcts.opal.controllers;

import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.opal.AbstractIntegrationTest;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

abstract class AbstractContentDigestIntegrationTest extends AbstractIntegrationTest {

    protected static final String CONTENT_DIGEST = "Content-Digest";
    protected static final String ROOT_ENDPOINT = "/";

    private static final byte[] EMPTY_BODY = new byte[0];

    protected static String validEmptyBodyDigest() throws NoSuchAlgorithmException {
        return contentDigestHeaderFor(EMPTY_BODY);
    }

    protected static String invalidDigest() throws NoSuchAlgorithmException {
        return contentDigestHeaderFor("different".getBytes(StandardCharsets.UTF_8));
    }

    protected static String malformedDigest() {
        return "not-a-digest";
    }

    protected static void assertContentDigestProblem(MvcResult result, String title, String detail) throws Exception {
        jsonPath("$.type").value("https://hmcts.gov.uk/problems/content-digest").match(result);
        jsonPath("$.title").value(title).match(result);
        jsonPath("$.detail").value(detail).match(result);
    }

    protected static void assertValidResponseDigest(MvcResult result) throws NoSuchAlgorithmException {
        byte[] body = result.getResponse().getContentAsByteArray();
        assertThat(result.getResponse().getHeader(CONTENT_DIGEST)).isEqualTo(contentDigestHeaderFor(body));
    }

    private static String contentDigestHeaderFor(byte[] content) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-512");
        String digest = Base64.getEncoder().encodeToString(messageDigest.digest(content));
        return "sha-512=:" + digest + ":";
    }
}
