package uk.gov.hmcts.opal.dto.legacy;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.dto.ToJsonString;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j(topic = "opal.DefendantAccountsSearchResultsTest")
class DefendantAccountsSearchResultsTest {

    @Disabled("Should LegacyDefendantAccountsSearchResults be just XML, not JSON?")
    @Test
    void testJsonString() throws Exception {
        LegacyDefendantAccountsSearchResults model = constructTestDefendantAccountsSearchResults();
        assertNotNull(model.toJsonString());

        LegacyDefendantAccountsSearchResults parsed = ToJsonString.getObjectMapper()
            .readValue(getJsonRepresentation(), LegacyDefendantAccountsSearchResults.class);
        assertNotNull(parsed);
    }


    private LegacyDefendantAccountsSearchResults constructTestDefendantAccountsSearchResults() {
        return LegacyDefendantAccountsSearchResults.builder()
            .totalCount(1L)
            .defendantAccountsSearchResult(List.of(LegacyDefendantAccountSearchResult.builder().build()))
            .build();
    }

    private String getJsonRepresentation() {
        return """
            {
              "defendantAccountsSearchResult" : [ {
                "accountNumber" : "accountNo",
                "organisation" : null,
                "title" : "Mr",
                "surname" : "Smith",
                "forenames" : "John",
                "rowNumber" : null,
                "defendant_account_id" : 12345,
                "business_unit_id" : 9,
                "business_unit_name" : "Cardiff",
                "organisation_name" : null,
                "birth_date" : "1977-06-26",
                "address_line_1" : "Scotland",
                "account_balance" : 1000
              } ],
              "totalCount" : 1
            }
            """;
    }
}
