package uk.gov.hmcts.opal.steps.refdata;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.opal.support.refdata.LjaApiFixture.withoutCredentialLogging;
import static uk.gov.hmcts.opal.support.refdata.LjaConsumptionContext.current;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.serenitybdd.core.Serenity;
import uk.gov.hmcts.opal.steps.BaseStepDef;
import uk.gov.hmcts.opal.support.refdata.LjaApiFixture;
import uk.gov.hmcts.opal.support.refdata.LjaMessageFixture;

/** The manual publisher -> production GET workflow, using one scenario-owned key. */
public class LjaConsumptionStepDef extends BaseStepDef {
    @Given("a new LJA request is loaded from the publication JSON fixture")
    public void loadNewLjaRequest() {
        assertThat(current().id).as("Allocate one LJA per scenario").isNull();
        current().id = current().api.allocate();
        current().lastRecords = List.of(LjaMessageFixture.fromFixture(current().id,
            current().resolve("<unique:created:name>")));
    }

    @Given("an LJA from the publication JSON fixture has been created and verified")
    public void createOwnedBaseline() {
        loadNewLjaRequest();
        postLjaRequest();
        assertPublishedStatus(202);
        assertPublishedRecord();
    }

    @When("I POST the LJA request to the reference data topic")
    public void postLjaRequest() {
        assertThat(current().pending).as("Observe pending work before publishing another message").isEmpty();
        var before = current().api.find(current().id);
        before.ifPresent(row -> {
            assertThat(current().observed).as("Amend only an observed scenario-created LJA").isNotNull();
            assertThat(row.name()).as("Existing row must retain our last observed ownership marker")
                .isEqualTo(current().observed.name());
            assertThat(current().lastRecords.getFirst().name()).as("Each update needs a fresh processing marker")
                .isNotEqualTo(row.name());
        });
        current().lastMessage = LjaMessageFixture.create(current().lastRecords);
        // Register before POST: a transport failure can occur after asynchronous acceptance.
        current().pending.put(current().lastMessage.id(), current().lastRecords.getFirst());
        Serenity.recordReportData().withTitle("LJA publication request").andContents(current().evidence());
        current().lastResponse = withoutCredentialLogging(authorisedJsonRequest()).body(current().lastMessage.body())
            .post(getTestUrl() + "/testing-support/ref-data/topic");
        if (current().lastResponse.statusCode() == 400) {
            current().pending.remove(current().lastMessage.id());
        }
    }

    @When("I POST these changes to the same LJA")
    public void postLjaUpdate(DataTable overrides) {
        assertThat(current().observed).as("Update only the LJA created by this scenario").isNotNull();
        Map<String, String> changes = new LinkedHashMap<>();
        overrides.asMaps().forEach(row -> changes.put(row.get("field"), current().resolve(row.get("value"))));
        current().lastRecords = List.of(LjaMessageFixture.withOverrides(current().lastRecords.getFirst(), changes));
        postLjaRequest();
    }

    @When("I POST the LJA request with lja_name set to null")
    public void postWithNullName() {
        current().lastRecords = List.of(LjaMessageFixture.withOverrides(current().lastRecords.getFirst(),
            Map.of("lja_name", "<null>")));
        postLjaRequest();
    }

    @Then("the LJA publication response status is {int}")
    public void assertPublishedStatus(int expected) {
        assertThat(current().lastResponse.statusCode()).as(current().evidence()).isEqualTo(expected);
        if (expected == 400) {
            long deadline = System.nanoTime() + current().settings.observation().toNanos();
            do {
                current().api.assertAbsent(current().id);
                long remaining = deadline - System.nanoTime();
                if (remaining > 0) {
                    LjaApiFixture.pause(Duration.ofNanos(Math.min(current().settings.pollInterval().toNanos(),
                        remaining)));
                }
            } while (System.nanoTime() < deadline);
        }
    }

    @Then("the LJA GET response contains the expected values")
    public void assertPublishedRecord() {
        assertPublishedStatus(202);
        if (current().pending.containsKey(current().lastMessage.id())) {
            current().observe(current().lastMessage.id());
        }
        current().api.assertRow(LjaApiFixture.expected(current().id, current().lastRecords.getFirst()));
    }

    @Then("the LJA GET response end date is null")
    public void assertEndDateIsNull() {
        var actual = current().api.find(current().id);
        assertThat(actual).as(current().evidence()).isPresent();
        assertThat(actual.orElseThrow().endDate()).as("Present endDate must be null; " + current().evidence())
            .isNull();
    }

    @Then("no LJAs exist for {string}")
    public void assertNoLjas(String alias) {
        assertThat(alias).as("The scenario-owned LJA alias").isEqualTo("owned");
        current().api.assertAbsent(current().id);
    }
}
