package uk.gov.hmcts.opal.service.legacy;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.StreamingHttpOutputMessage;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.nio.charset.Charset;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.mockito.Mockito.spy;


public class MockRestClient implements RestClient {

    public MockRequestHeadersUriSpec requestHeadersUriSpec = spy(new MockRequestHeadersUriSpec());

    public MockRequestBodyUriSpec requestBodyUriSpec = spy(new MockRequestBodyUriSpec());

    public MockResponseSpec responseSpec = spy(new MockResponseSpec());


    @Override
    public RequestHeadersUriSpec<?> get() {
        return requestHeadersUriSpec;
    }

    @Override
    public RequestHeadersUriSpec<?> head() {
        return requestHeadersUriSpec;
    }

    @Override
    public RequestBodyUriSpec post() {
        return requestBodyUriSpec;
    }

    @Override
    public RequestBodyUriSpec put() {
        return requestBodyUriSpec;
    }

    @Override
    public RequestBodyUriSpec patch() {
        return requestBodyUriSpec;
    }

    @Override
    public RequestHeadersUriSpec<?> delete() {
        return requestHeadersUriSpec;
    }

    @Override
    public RequestHeadersUriSpec<?> options() {
        return requestHeadersUriSpec;
    }

    @Override
    public RequestBodyUriSpec method(HttpMethod method) {
        return requestBodyUriSpec;
    }

    @Override
    public Builder mutate() {
        return null;
    }

    public class MockRequestHeadersSpec implements RequestHeadersSpec {

        @Override
        public RequestHeadersSpec accept(MediaType... acceptableMediaTypes) {
            return requestHeadersUriSpec;
        }

        @Override
        public RequestHeadersSpec acceptCharset(Charset... acceptableCharsets) {
            return requestHeadersUriSpec;
        }

        @Override
        public RequestHeadersSpec ifModifiedSince(ZonedDateTime ifModifiedSince) {
            return requestHeadersUriSpec;
        }

        @Override
        public RequestHeadersSpec ifNoneMatch(String... ifNoneMatches) {
            return requestHeadersUriSpec;
        }

        @Override
        public RequestHeadersSpec header(String headerName, String... headerValues) {
            return requestHeadersUriSpec;
        }

        @Override
        public ResponseSpec retrieve() {
            return responseSpec;
        }

        @Override
        public Object exchange(ExchangeFunction exchangeFunction, boolean close) {
            return null;
        }

        @Override
        public RequestHeadersSpec httpRequest(Consumer consumer) {
            return requestHeadersUriSpec;
        }

        @Override
        public RequestHeadersSpec headers(Consumer consumer) {
            return requestHeadersUriSpec;
        }
    }

    public class MockRequestHeadersUriSpec extends MockRequestHeadersSpec implements RequestHeadersUriSpec {

        @Override
        public RequestHeadersSpec<?> uri(URI uri) {
            return requestHeadersUriSpec;
        }

        @Override
        public RequestHeadersSpec<?> uri(String uri, Object... uriVariables) {
            return requestHeadersUriSpec;
        }

        @Override
        public RequestHeadersSpec<?> uri(Function function) {
            return requestHeadersUriSpec;
        }

        @Override
        public RequestHeadersSpec<?> uri(String uri, Function function) {
            return requestHeadersUriSpec;
        }

        @Override
        public RequestHeadersSpec<?> uri(String uri, Map uriVariables) {
            return requestHeadersUriSpec;
        }
    }

    public class MockRequestBodySpec implements RequestBodySpec {
        @Override
        public RequestBodySpec contentLength(long contentLength) {
            return requestBodyUriSpec;
        }

        @Override
        public RequestBodySpec contentType(MediaType contentType) {
            return requestBodyUriSpec;
        }

        @Override
        public RequestBodySpec body(Object body) {
            return requestBodyUriSpec;
        }

        @Override
        public <T> RequestBodySpec body(T body, ParameterizedTypeReference<T> bodyType) {
            return requestBodyUriSpec;
        }

        @Override
        public RequestBodySpec body(StreamingHttpOutputMessage.Body body) {
            return requestBodyUriSpec;
        }

        @Override
        public RequestBodySpec accept(MediaType... acceptableMediaTypes) {
            return requestBodyUriSpec;
        }

        @Override
        public RequestBodySpec acceptCharset(Charset... acceptableCharsets) {
            return requestBodyUriSpec;
        }

        @Override
        public RequestBodySpec ifModifiedSince(ZonedDateTime ifModifiedSince) {
            return requestBodyUriSpec;
        }

        @Override
        public RequestBodySpec ifNoneMatch(String... ifNoneMatches) {
            return requestBodyUriSpec;
        }

        @Override
        public RequestBodySpec header(String headerName, String... headerValues) {
            return requestBodyUriSpec;
        }

        @Override
        public RequestBodySpec headers(Consumer<HttpHeaders> headersConsumer) {
            return requestBodyUriSpec;
        }

        @Override
        public RequestBodySpec httpRequest(Consumer<ClientHttpRequest> requestConsumer) {
            return requestBodyUriSpec;
        }

        @Override
        public ResponseSpec retrieve() {
            return responseSpec;
        }

        @Override
        public <T> T exchange(ExchangeFunction<T> exchangeFunction, boolean close) {
            return null;
        }
    }

    public class MockRequestBodyUriSpec extends MockRequestBodySpec implements RequestBodyUriSpec {

        @Override
        public RequestBodySpec uri(URI uri) {
            return requestBodyUriSpec;
        }

        @Override
        public RequestBodySpec uri(String uri, Object... uriVariables) {
            return requestBodyUriSpec;
        }

        @Override
        public RequestBodySpec uri(String uri, Map<String, ?> uriVariables) {
            return requestBodyUriSpec;
        }

        @Override
        public RequestBodySpec uri(String uri, Function<UriBuilder, URI> uriFunction) {
            return requestBodyUriSpec;
        }

        @Override
        public RequestBodySpec uri(Function<UriBuilder, URI> uriFunction) {
            return requestBodyUriSpec;
        }
    }

    public class MockResponseSpec implements ResponseSpec {

        @Override
        public ResponseSpec onStatus(Predicate<HttpStatusCode> statusPredicate, ErrorHandler errorHandler) {
            return responseSpec;
        }

        @Override
        public ResponseSpec onStatus(ResponseErrorHandler errorHandler) {
            return responseSpec;
        }

        @Override
        public <T> T body(Class<T> bodyType) {
            return null;
        }

        @Override
        public <T> T body(ParameterizedTypeReference<T> bodyType) {
            return null;
        }

        @Override
        public <T> ResponseEntity<T> toEntity(Class<T> bodyType) {
            return null;
        }

        @Override
        public <T> ResponseEntity<T> toEntity(ParameterizedTypeReference<T> bodyType) {
            return null;
        }

        @Override
        public ResponseEntity<Void> toBodilessEntity() {
            return null;
        }
    }
}
