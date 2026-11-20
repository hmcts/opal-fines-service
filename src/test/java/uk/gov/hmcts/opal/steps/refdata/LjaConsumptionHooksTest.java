package uk.gov.hmcts.opal.steps.refdata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.cucumber.java.Scenario;
import io.restassured.builder.ResponseBuilder;
import io.restassured.specification.RequestSpecification;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.support.refdata.LjaApiFixture;
import uk.gov.hmcts.opal.support.refdata.LjaConsumptionContext;
import uk.gov.hmcts.opal.support.refdata.LjaMessageFixture;

class LjaConsumptionHooksTest {
    private LjaConsumptionContext.State state;
    private RequestSpecification requests;
    private LjaMessageFixture.RecordData original;
    private Scenario scenario;

    @BeforeEach
    void setup() {
        LjaConsumptionContext.reset();
        state = LjaConsumptionContext.current();
        state.id = 42;
        state.settings = new LjaConsumptionContext.Settings(Duration.ofMillis(20), Duration.ofMillis(1),
            Duration.ofMillis(2));
        state.api = mock(LjaApiFixture.class);
        original = LjaMessageFixture.fromFixture((short) 42, "Unique original");
        state.lastRecords = List.of(original);
        requests = mock(RequestSpecification.class);
        when(requests.delete(anyString())).thenReturn(new ResponseBuilder().setStatusCode(204).build());
        scenario = mock(Scenario.class);
    }

    @AfterEach
    void clear() {
        LjaConsumptionContext.clear();
    }

    @Test
    void observesPendingCreateBeforeDeletingOwnedKey() {
        UUID id = pending(original);
        var expected = new LjaApiFixture.Marker((short) 42, original.name());
        when(state.api.awaitMarker((short) 42, original.name(), state.settings)).thenReturn(expected);
        when(state.api.findOwned((short) 42)).thenReturn(Optional.of(expected));

        cleanup().afterScenario(scenario);

        var order = inOrder(state.api, requests);
        order.verify(state.api).awaitMarker((short) 42, original.name(), state.settings);
        order.verify(state.api).findOwned((short) 42);
        order.verify(requests).delete(anyString());
        order.verify(state.api).assertAbsent((short) 42);
        assertThat(state.pending).doesNotContainKey(id);
        assertThat(LjaConsumptionContext.current().id).isNull();
    }

    @Test
    void unresolvedCreateRetainsAndReportsUuidWithoutMaskingPrimaryFailure() {
        UUID id = pending(original);
        when(state.api.awaitMarker((short) 42, original.name(), state.settings))
            .thenThrow(new AssertionError("No marker; last GET absent"));
        when(scenario.isFailed()).thenReturn(true);

        assertThatCode(() -> cleanup().afterScenario(scenario)).doesNotThrowAnyException();

        verify(requests, never()).delete(anyString());
        verify(scenario).log(contains(id.toString()));
        verify(scenario).attach(anyString(), anyString(), contains("42"));
        assertThat(state.pending).containsKey(id);
        assertThat(LjaConsumptionContext.current().id).isNull();
    }

    @Test
    void deletesObservedOwnedUpdateWithoutRestorationPublication() {
        var changed = LjaMessageFixture.withOverrides(original, Map.of("lja_name", "Unique changed"));
        state.observed = changed;
        when(state.api.findOwned((short) 42))
            .thenReturn(Optional.of(new LjaApiFixture.Marker((short) 42, changed.name())));

        cleanup().afterScenario(scenario);

        var order = inOrder(requests, state.api);
        order.verify(requests).delete(anyString());
        order.verify(state.api).assertAbsent((short) 42);
        verify(requests, never()).post(anyString());
    }

    @Test
    void unownedExistingRowFailsWithoutDelete() {
        when(state.api.findOwned((short) 42))
            .thenReturn(Optional.of(new LjaApiFixture.Marker((short) 42, original.name())));
        assertThatThrownBy(() -> cleanup().afterScenario(scenario)).isInstanceOf(IllegalStateException.class);
        verify(requests, never()).delete(anyString());
    }

    @Test
    void changedOwnershipMarkerFailsWithoutDelete() {
        state.observed = original;
        var foreign = LjaMessageFixture.withOverrides(original, Map.of("lja_name", "Another publisher"));
        when(state.api.findOwned((short) 42))
            .thenReturn(Optional.of(new LjaApiFixture.Marker((short) 42, foreign.name())));
        assertThatThrownBy(() -> cleanup().afterScenario(scenario)).isInstanceOf(IllegalStateException.class);
        verify(requests, never()).delete(anyString());
    }

    @Test
    void unresolvedUpdateRetainsPublicationAndDoesNotDelete() {
        state.observed = original;
        var changed = LjaMessageFixture.withOverrides(original, Map.of("lja_name", "Changed"));
        UUID publication = pending(changed);
        when(state.api.awaitMarker((short) 42, changed.name(), state.settings))
            .thenThrow(new AssertionError("Update not observed"));

        assertThatThrownBy(() -> cleanup().afterScenario(scenario)).isInstanceOf(IllegalStateException.class)
            .satisfies(failure -> assertThat(failure.getSuppressed()).hasSize(1));
        verify(requests, never()).delete(anyString());
        assertThat(state.pending).containsKey(publication);
        verify(scenario).log(contains(publication.toString()));
    }

    @Test
    void failedAbsenceConfirmationIsReported() {
        state.observed = original;
        when(state.api.findOwned((short) 42))
            .thenReturn(Optional.of(new LjaApiFixture.Marker((short) 42, original.name())));
        doThrow(new AssertionError("Row still exists")).when(state.api).assertAbsent((short) 42);
        assertThatThrownBy(() -> cleanup().afterScenario(scenario)).isInstanceOf(IllegalStateException.class);
        verify(scenario).attach(contains("Row still exists"), anyString(), contains("42"));
    }

    private UUID pending(LjaMessageFixture.RecordData record) {
        UUID id = UUID.randomUUID();
        state.lastMessage = new LjaMessageFixture.Message(id, Map.of());
        state.lastRecords = List.of(record);
        state.pending.put(id, record);
        return id;
    }

    private LjaConsumptionHooks cleanup() {
        return new LjaConsumptionHooks() {
            @Override
            protected RequestSpecification cleanupRequest() {
                return requests;
            }
        };
    }
}
