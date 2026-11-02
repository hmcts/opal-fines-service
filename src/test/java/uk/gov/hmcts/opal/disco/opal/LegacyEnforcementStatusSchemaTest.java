package uk.gov.hmcts.opal.disco.opal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;

class LegacyEnforcementStatusSchemaTest {

    private final JsonSchemaValidationService validationService = new JsonSchemaValidationService();

    @ParameterizedTest(name = "{0}")
    @MethodSource("enforcementCourts")
    void validateEnforcementCourt(String description, String courtProperty, boolean valid) {
        String response = """
            {
              "version": "1",
              "employer_flag": false,
              "account_status_reference": { "account_status_code": "L" },
              "enforcement_overview": {
                "days_in_default": 0,
                "collection_order": { "collection_order_flag": false }%s
              }
            }
            """.formatted(courtProperty);

        Set<String> errors = validationService.validate(response,
            "legacy/getDefendantAccountEnforcementStatusLegacyResponse.json");

        assertEquals(valid, errors.isEmpty(), () -> description + ": " + errors);
    }

    private static Stream<Arguments> enforcementCourts() {
        return Stream.of(
            Arguments.of("omitted court is valid (PO-11108)", "", true),
            Arguments.of("populated court remains valid",
                ", \"enforcement_court\": { \"court_id\": 130000000001, \"court_name\": \"Test Court\" }", true),
            Arguments.of("present court still requires its fields", ", \"enforcement_court\": {}", false)
        );
    }
}
