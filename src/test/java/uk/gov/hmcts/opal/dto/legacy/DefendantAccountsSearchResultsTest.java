package uk.gov.hmcts.opal.dto.legacy;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.dto.ToJsonString;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j(topic = "opal.DefendantAccountsSearchResultsTest")
public class DefendantAccountsSearchResultsTest {

    @Test
    void testBuilder() {
        DefendantAccountsSearchResults searchResults = constructTestDefendantAccountsSearchResults();

        assertEquals(1L, searchResults.getTotalCount());
        assertNotNull(searchResults.getDefendantAccountsSearchResult());
        assertEquals(1, searchResults.getDefendantAccountsSearchResult().size());
        assertEquals(
            DefendantAccountSearchResultTest.constructTestDefendantAccountSearchResult(),
            searchResults.getDefendantAccountsSearchResult().get(0)
        );

    }

    @Test
    void testJsonString() throws Exception {
        DefendantAccountsSearchResults model = constructTestDefendantAccountsSearchResults();
        assertNotNull(model.toJsonString());

        DefendantAccountsSearchResults parsed = ToJsonString.getObjectMapper()
            .readValue(getJsonRepresentation(), DefendantAccountsSearchResults.class);
        assertNotNull(parsed);
    }


    private DefendantAccountsSearchResults constructTestDefendantAccountsSearchResults() {
        return DefendantAccountsSearchResults.builder()
            .totalCount(1L)
            .defendantAccountsSearchResult(List.of(DefendantAccountSearchResultTest
                                                       .constructTestDefendantAccountSearchResult()))
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
