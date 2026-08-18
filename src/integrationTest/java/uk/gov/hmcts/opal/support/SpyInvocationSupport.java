package uk.gov.hmcts.opal.support;

import static org.mockito.Mockito.mockingDetails;

public final class SpyInvocationSupport {

    private SpyInvocationSupport() {
    }

    public static long countInvocationsByMethodName(Object spy, String methodName) {
        return mockingDetails(spy).getInvocations().stream()
            .filter(invocation -> invocation.getMethod().getName().equals(methodName))
            .count();
    }
}
