package uk.gov.hmcts.opal.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import uk.gov.hmcts.opal.utils.TestHttpClient;
import uk.gov.hmcts.opal.utils.TestHttpClient.TestHttpResponse;

class BearerTokenStepDefTest {

    private static final String USER = "test-user";
    private static final TestHttpResponse SUCCESS = new TestHttpResponse(200, "{\"access_token\":\"token\"}");

    @Test
    void returnsTokenWithoutRetry() {
        try (MockedStatic<TestHttpClient> client = mockStatic(TestHttpClient.class)) {
            client.when(() -> TestHttpClient.get(anyString(), anyMap())).thenReturn(SUCCESS);

            assertThat(BearerTokenStepDef.getAccessTokenForUser(USER)).isEqualTo("token");

            client.verify(() -> TestHttpClient.get(anyString(), anyMap()), times(1));
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {502, 503, 504})
    void recoversFromTransientFailure(int status) {
        try (MockedStatic<TestHttpClient> client = mockStatic(TestHttpClient.class)) {
            client.when(() -> TestHttpClient.get(anyString(), anyMap()))
                .thenReturn(new TestHttpResponse(status, ""), SUCCESS);

            assertThat(BearerTokenStepDef.getAccessTokenForUser(USER)).isEqualTo("token");

            client.verify(() -> TestHttpClient.get(anyString(), anyMap()), times(2));
        }
    }

    @Test
    void failsAfterFourAttempts() {
        try (MockedStatic<TestHttpClient> client = mockStatic(TestHttpClient.class)) {
            client.when(() -> TestHttpClient.get(anyString(), anyMap())).thenReturn(new TestHttpResponse(502, ""));

            assertThatThrownBy(() -> BearerTokenStepDef.getAccessTokenForUser(USER))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Failed to fetch access token, status: 502 after 4 attempt(s)");

            client.verify(() -> TestHttpClient.get(anyString(), anyMap()), times(4));
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {400, 401, 403, 404, 500})
    void failsImmediatelyForOtherStatuses(int status) {
        try (MockedStatic<TestHttpClient> client = mockStatic(TestHttpClient.class)) {
            client.when(() -> TestHttpClient.get(anyString(), anyMap())).thenReturn(new TestHttpResponse(status, ""));

            assertThatThrownBy(() -> BearerTokenStepDef.getAccessTokenForUser(USER))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("status: " + status + " after 1 attempt(s)");

            client.verify(() -> TestHttpClient.get(anyString(), anyMap()), times(1));
        }
    }

    @Test
    void stopsRetryingWhenInterrupted() {
        try (MockedStatic<TestHttpClient> client = mockStatic(TestHttpClient.class)) {
            client.when(() -> TestHttpClient.get(anyString(), anyMap())).thenReturn(new TestHttpResponse(503, ""));
            Thread.currentThread().interrupt();

            assertThatThrownBy(() -> BearerTokenStepDef.getAccessTokenForUser(USER))
                .isInstanceOf(IllegalStateException.class)
                .hasCauseInstanceOf(InterruptedException.class);
            assertThat(Thread.currentThread().isInterrupted()).isTrue();

            client.verify(() -> TestHttpClient.get(anyString(), anyMap()), times(1));
        } finally {
            Thread.interrupted();
        }
    }
}
