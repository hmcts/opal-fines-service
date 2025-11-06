package uk.gov.hmcts.opal.service.legacy;

import org.springframework.http.HttpStatusCode;
import org.springframework.scheduling.annotation.Async;
import uk.gov.hmcts.opal.dto.legacy.HasErrorResponse;

import java.util.concurrent.CompletableFuture;

public interface GatewayService {

    <T> Response<T> postToGateway(String actionType, Class<T> responseType, Object request, String responseSchemaFile);

    @Async
    <T> CompletableFuture<Response<T>> postToGatewayAsync(
        String actionType, Class<T> responseType, Object request, String responseSchemaFile);

    <T> Response<T> patchToGateway(String actionType, Class<T> responseType, Object request, String responseSchemaFile);

    <T> CompletableFuture<Response<T>> patchToGatewayAsync(
        String actionType, Class<T> responseType, Object request, String responseSchemaFile);

    class Response<T> {
        public final HttpStatusCode code;
        public final T responseEntity;
        public final String body;
        public final Throwable exception;

        public Response(HttpStatusCode code, T responseEntity) {
            this(code, responseEntity, null, null);
        }

        public Response(HttpStatusCode code, String body) {
            this(code, null, body, null);
        }

        public Response(HttpStatusCode code, Throwable exception, String body) {
            this(code, null, body, exception);
        }

        public Response(HttpStatusCode code, T responseEntity, String body, Throwable exception) {
            this.code = code;
            this.responseEntity = responseEntity;
            this.body = body;
            this.exception = exception;
        }

        public boolean isSuccessful() {
            return code.is2xxSuccessful();
        }

        public boolean isException() {
            return exception != null;
        }

        public boolean isError() {
            return isException() || code.isError() || hasErrorResponse();
        }

        public boolean hasErrorResponse() {
            return responseEntity != null
                && responseEntity instanceof HasErrorResponse hasErrorResponse
                && hasErrorResponse.getErrorResponse() != null;
        }

        public boolean isLegacyFailure() {
            return code.is5xxServerError();
        }
    }
}
