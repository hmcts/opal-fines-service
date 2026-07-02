package uk.gov.hmcts.opal.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import feign.Request;
import feign.RequestTemplate;
import feign.RetryableException;
import feign.Retryer;
import java.net.SocketTimeoutException;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class ReadOnlyFeignRetryerTest {

    @Test
    void continueOrPropagate_delegatesForGetRequests() {
        RecordingRetryer delegate = new RecordingRetryer();
        ReadOnlyFeignRetryer retryer = new ReadOnlyFeignRetryer(delegate);

        assertDoesNotThrow(() -> retryer.continueOrPropagate(retryableException(Request.HttpMethod.GET)));

        assertThat(delegate.attempts).isEqualTo(1);
    }

    @Test
    void continueOrPropagate_propagatesForPostRequests() {
        RecordingRetryer delegate = new RecordingRetryer();
        ReadOnlyFeignRetryer retryer = new ReadOnlyFeignRetryer(delegate);
        RetryableException exception = retryableException(Request.HttpMethod.POST);

        assertThatThrownBy(() -> retryer.continueOrPropagate(exception)).isSameAs(exception);

        assertThat(delegate.attempts).isZero();
    }

    @Test
    void clone_usesClonedDelegate() {
        RecordingRetryer delegate = new RecordingRetryer();
        RecordingRetryer clonedDelegate = new RecordingRetryer();
        delegate.clone = clonedDelegate;

        Retryer retryer = new ReadOnlyFeignRetryer(delegate).clone();

        assertDoesNotThrow(() -> retryer.continueOrPropagate(retryableException(Request.HttpMethod.GET)));

        assertThat(delegate.attempts).isZero();
        assertThat(clonedDelegate.attempts).isEqualTo(1);
    }

    private static RetryableException retryableException(Request.HttpMethod method) {
        return new RetryableException(
            -1,
            "Read timed out",
            method,
            new SocketTimeoutException("Read timed out"),
            (Long) null,
            request(method)
        );
    }

    private static Request request(Request.HttpMethod method) {
        return Request.create(
            method,
            "http://user-service/opal/v2/users/0/state",
            Map.of(),
            (byte[]) null,
            null,
            new RequestTemplate()
        );
    }

    private static class RecordingRetryer implements Retryer {
        private int attempts;
        private Retryer clone = this;

        @Override
        public void continueOrPropagate(RetryableException e) {
            attempts++;
        }

        @Override
        public Retryer clone() {
            return clone;
        }
    }
}
