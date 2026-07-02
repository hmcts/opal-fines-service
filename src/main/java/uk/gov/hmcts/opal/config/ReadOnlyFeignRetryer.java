package uk.gov.hmcts.opal.config;

import feign.Request;
import feign.RetryableException;
import feign.Retryer;
import java.util.Set;

public class ReadOnlyFeignRetryer implements Retryer {

    private static final Set<Request.HttpMethod> RETRIABLE_METHODS = Set.of(
        Request.HttpMethod.GET,
        Request.HttpMethod.HEAD
    );

    private final Retryer delegate;

    public ReadOnlyFeignRetryer() {
        this(new Retryer.Default(100, 1000, 2));
    }

    public ReadOnlyFeignRetryer(Retryer delegate) {
        this.delegate = delegate;
    }

    @Override
    public void continueOrPropagate(RetryableException e) {
        if (!RETRIABLE_METHODS.contains(e.method())) {
            throw e;
        }

        delegate.continueOrPropagate(e);
    }

    @Override
    public Retryer clone() {
        return new ReadOnlyFeignRetryer(delegate.clone());
    }
}
