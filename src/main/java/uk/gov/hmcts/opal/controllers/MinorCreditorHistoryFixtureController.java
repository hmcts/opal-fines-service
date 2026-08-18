package uk.gov.hmcts.opal.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.service.opal.MinorCreditorHistoryFixtureService;
import uk.gov.hmcts.opal.service.opal.MinorCreditorHistoryFixtureService.MinorCreditorHistoryFixture;

@Hidden
@RestController
@RequestMapping("/testing-support/minor-creditor-history")
@RequiredArgsConstructor
@Slf4j(topic = "opal.MinorCreditorHistoryFixtureController")
@ConditionalOnProperty(prefix = "opal.testing-support-endpoints", name = "enabled", havingValue = "true")
public class MinorCreditorHistoryFixtureController {

    private final MinorCreditorHistoryFixtureService fixtureService;

    /**
     * Creates a minor-creditor account and representative history records for functional tests.
     *
     * @param request optional fixture reference.
     * @return identifiers and date window for the created fixture.
     */
    @PostMapping
    public ResponseEntity<MinorCreditorHistoryFixture> createFixture(
        @RequestBody(required = false) MinorCreditorHistoryFixtureRequest request) {

        String reference = request == null ? null : request.reference();
        MinorCreditorHistoryFixture fixture = fixtureService.createFixture(reference);
        log.warn("TEST ENDPOINT: Created minor-creditor history fixture {}", fixture.creditorAccountId());
        return ResponseEntity.ok(fixture);
    }

    /**
     * Deletes a minor-creditor history fixture created for functional tests.
     *
     * @param creditorAccountId creditor account id returned by {@link #createFixture}.
     * @return no-content response after deletion.
     */
    @DeleteMapping("/{creditorAccountId}")
    public ResponseEntity<Void> deleteFixture(@PathVariable Long creditorAccountId) {
        log.warn("TEST ENDPOINT: Deleting minor-creditor history fixture {}", creditorAccountId);
        fixtureService.deleteFixture(creditorAccountId);
        return ResponseEntity.noContent().build();
    }

    public record MinorCreditorHistoryFixtureRequest(@JsonProperty("reference") String reference) {
    }
}
