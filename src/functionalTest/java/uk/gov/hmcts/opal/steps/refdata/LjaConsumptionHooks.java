package uk.gov.hmcts.opal.steps.refdata;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.opal.support.refdata.LjaApiFixture.withoutCredentialLogging;
import static uk.gov.hmcts.opal.support.refdata.LjaConsumptionContext.current;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.restassured.specification.RequestSpecification;
import java.util.ArrayList;
import java.util.List;
import uk.gov.hmcts.opal.steps.BaseStepDef;
import uk.gov.hmcts.opal.support.refdata.LjaApiFixture;
import uk.gov.hmcts.opal.support.refdata.LjaConsumptionContext;

/** Runs before generic DataCleanUp (MIN_VALUE), retaining its current authenticated user. */
public class LjaConsumptionHooks extends BaseStepDef {
    @Before(value = "@LjaMessageConsumption", order = 10)
    public void beforeScenario() {
        LjaConsumptionContext.reset();
        current().settings = LjaConsumptionContext.Settings.load();
        current().api = new LjaApiFixture(getTestUrl(), this::authorisedJsonRequest);
    }

    @After(value = "@LjaMessageConsumption", order = 10)
    public void afterScenario(Scenario scenario) {
        List<Throwable> failures = new ArrayList<>();
        try {
            if (current().api == null) {
                return;
            }
            if (current().id != null) {
                short id = current().id;
                try {
                    cleanupOwned(id, scenario);
                } catch (Exception | AssertionError failure) {
                    failures.add(failure);
                    scenario.log("LJA cleanup retained affected ID " + id + "; unresolved publication UUIDs/IDs: "
                        + current().pending + "; " + current().evidence());
                    scenario.attach(failure.toString(), "text/plain", "LJA cleanup failure for " + id);
                }
            }
        } finally {
            LjaConsumptionContext.clear();
        }
        if (!failures.isEmpty() && !scenario.isFailed()) {
            IllegalStateException failure = new IllegalStateException("LJA cleanup failed; affected IDs retained");
            failures.forEach(failure::addSuppressed);
            throw failure;
        }
    }

    private void cleanupOwned(short id, Scenario scenario) {
        // Never DELETE below accepted or transport-uncertain work that has not appeared in production GET.
        for (var messageId : List.copyOf(current().pending.keySet())) {
            current().observe(messageId);
        }
        var actual = current().api.findOwned(id);
        if (actual.isEmpty()) {
            return;
        }
        assertThat(current().observed).as("Delete only a positively observed scenario-created LJA").isNotNull();
        assertThat(actual.get().name()).as("Delete only the last observed scenario-owned marker")
            .isEqualTo(current().observed.name());
        // Full-contract failure still permits deletion after the exact ownership marker has been observed.
        assertThat(cleanupRequest().delete(getTestUrl() + "/testing-support/ref-data/LJA/" + id).statusCode())
            .as("Delete owned LJA %s", id).isEqualTo(204);
        current().api.assertAbsent(id);
        scenario.log("Owned LJA " + id + ": DELETE 204 and genuine entity-not-found 404 confirmed");
    }

    protected RequestSpecification cleanupRequest() {
        return withoutCredentialLogging(authorisedJsonRequest());
    }
}
