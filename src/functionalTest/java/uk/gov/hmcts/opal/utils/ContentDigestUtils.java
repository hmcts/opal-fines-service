package uk.gov.hmcts.opal.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public final class ContentDigestUtils {

    private static final byte[] EMPTY_BODY = new byte[0];

    private ContentDigestUtils() {
    }

    public static String contentDigestHeaderForEmptyBody() {
        return contentDigestHeaderFor(EMPTY_BODY);
    }

    public static String invalidContentDigestHeader() {
        return contentDigestHeaderFor("different".getBytes(StandardCharsets.UTF_8));
    }

    public static String malformedContentDigestHeader() {
        return "not-a-digest";
    }

    private static String contentDigestHeaderFor(byte[] content) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-512");
            String digest = Base64.getEncoder().encodeToString(messageDigest.digest(content));
            return "sha-512=:" + digest + ":";
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-512 digest algorithm is not available", e);
        }
    }
}
