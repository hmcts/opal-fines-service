package uk.gov.hmcts.opal.utils;

import static io.restassured.config.HeaderConfig.headerConfig;
import static uk.gov.hmcts.opal.steps.BaseStepDef.getTestUrl;
import static uk.gov.hmcts.opal.steps.BearerTokenStepDef.getToken;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Locale;
import org.htmlunit.jetty.http.HttpHeader;

public class RequestSupport {

    public static RequestSpecBuilder generalRequestSpec(
        String path
    ) {
        return new RequestSpecBuilder()
            .setConfig(
                RestAssuredConfig.config()
                    .headerConfig(
                        headerConfig()
                            .overwriteHeadersWithName(
                                HttpHeader.AUTHORIZATION.toString(),
                                HttpHeader.CONTENT_TYPE.toString(),
                                HttpHeader.ACCEPT.toString(),
                                Constants.CONTENT_DIGEST_HEADER
                            )))
            .setBaseUri(getTestUrl())
            .setBasePath(path)
            .log(LogDetail.ALL)
            .addHeader(HttpHeader.AUTHORIZATION.toString(), "Bearer " + getToken())
            .setContentType(ContentType.JSON)
            .setAccept(ContentType.JSON);
    }

    public static RequestSpecBuilder patchRequestSpec(
        String path,
        String body
    ) {
        return postRequestSpec(path, body);
    }

    public static RequestSpecBuilder putRequestSpec(
        String path,
        String body
    ) {
        return postRequestSpec(path, body);
    }

    public static RequestSpecBuilder postRequestSpec(
        String path,
        String body
    ) {
        return generalRequestSpec(path)
            .setBody(body)
            .addHeader(Constants.CONTENT_DIGEST_HEADER, RequestSupport.getContentDigest(body));
    }

    public static RequestSpecBuilder getRequestSpec(
        String path
    ) {
        return generalRequestSpec(path);
    }

    public static String getContentDigest(String body) {
        String jcaAlgo = "SHA-256";
        String rfcToken = jcaAlgo.toLowerCase(Locale.ROOT);
        try {
            MessageDigest md = MessageDigest.getInstance(jcaAlgo);
            byte[] digest = md.digest(body.getBytes(StandardCharsets.UTF_8));
            String sfBinary = ":" + Base64.getEncoder().encodeToString(digest) + ":";
            return rfcToken + "=" + sfBinary;
        } catch (Exception ex) {
            throw new RuntimeException("Failed to generate Content-Digest", ex);
        }
    }

    public static ValidatableResponse responseProcessor(ValidatableResponse validatableResponse) {
        validatableResponse.log().body(true);
        return validatableResponse;
    }
}
