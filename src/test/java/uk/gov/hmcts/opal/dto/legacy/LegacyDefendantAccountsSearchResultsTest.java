package uk.gov.hmcts.opal.dto.legacy;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.dto.ToJsonString;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j(topic = "opal.LegacyDefendantAccountsSearchResultsTest")
public class LegacyDefendantAccountsSearchResultsTest {

    @Test
    void testBuilder() {
        LegacyDefendantAccountsSearchResults searchResults = constructTestDefendantAccountsSearchResults();

        assertEquals(1L, searchResults.getTotalCount());
        assertNotNull(searchResults.getDefendantAccountsSearchResult());
        assertEquals(1, searchResults.getDefendantAccountsSearchResult().size());
        assertEquals(
            LegacyDefendantAccountSearchResultTest.constructTestDefendantAccountSearchResult(),
            searchResults.getDefendantAccountsSearchResult().get(0)
        );

    }

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
            .defendantAccountsSearchResult(List.of(LegacyDefendantAccountSearchResultTest
                                                       .constructTestDefendantAccountSearchResult()))
            .build();
    }

    private String getJsonRepresentation() {
        return """
        {
          "defendant_accounts" : [ {
            "account_number" : "accountNo",
            "organisation" : false,
            "defendant_title" : "Mr",
            "defendant_surname" : "Smith",
            "defendant_firstnames" : "John",
            "defendant_account_id" : 12345,
            "business_unit_id" : "9",
            "business_unit_name" : "Cardiff",
            "organisation_name" : null,
            "birth_date" : "1977-06-26",
            "address_line_1" : "Scotland",
            "account_balance" : 1000
          } ],
          "count" : 1
        }
            """;
    }
}
