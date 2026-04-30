package uk.gov.hmcts.opal.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

/**
 * Lightweight HTTP client used by functional tests when a request needs to bypass SerenityRest.
 */
public final class TestHttpClient {

    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Utility class.
     */
    private TestHttpClient() {
    }

    /**
     * Sends a GET request to the supplied URL using the provided headers.
     *
     * @param url absolute URL to request.
     * @param headers request headers to include.
     * @return simplified response wrapper containing the status code and body.
     */
    public static TestHttpResponse get(String url, Map<String, String> headers) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET();

        headers.forEach(requestBuilder::header);

        return send(requestBuilder.build());
    }

    /**
     * Sends an arbitrary HTTP request to the supplied URL using the provided headers and optional
     * body.
     *
     * @param method HTTP method to execute.
     * @param url absolute URL to request.
     * @param headers request headers to include.
     * @param body request body to send, or {@code null} for a body-less request.
     * @return simplified response wrapper containing the status code and body.
     */
    public static TestHttpResponse request(String method, String url, Map<String, String> headers, String body) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .uri(URI.create(url));

        headers.forEach(requestBuilder::header);

        requestBuilder.method(method, body == null
            ? HttpRequest.BodyPublishers.noBody()
            : HttpRequest.BodyPublishers.ofString(body));

        return send(requestBuilder.build());
    }

    /**
     * Executes the prepared HTTP request and converts the Java HTTP client response into the
     * lightweight functional-test wrapper.
     *
     * @param request prepared HTTP request to execute.
     * @return simplified response wrapper containing the status code and body.
     */
    private static TestHttpResponse send(HttpRequest request) {
        try {
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            return new TestHttpResponse(response.statusCode(), response.body());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to call test endpoint", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while calling test endpoint", e);
        }
    }

    /**
     * Minimal response wrapper used by functional tests that rely on the raw HTTP client.
     *
     * @param statusCode HTTP status returned by the call.
     * @param body raw response body returned by the call.
     */
    public record TestHttpResponse(int statusCode, String body) {
        /**
         * Reads a top-level JSON field from the stored response body.
         *
         * @param fieldName top-level JSON field name to read.
         * @return field value as text, or {@code null} when the field is absent or explicitly null.
         */
        public String jsonPath(String fieldName) {
            try {
                JsonNode root = OBJECT_MAPPER.readTree(body);
                JsonNode node = root.path(fieldName);
                return node.isMissingNode() || node.isNull() ? null : node.asText();
            } catch (IOException e) {
                throw new IllegalStateException("Failed to parse JSON response body", e);
            }
        }
    }
}
