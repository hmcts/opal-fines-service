package uk.gov.hmcts.opal.dto.legacy.search;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.xml.bind.JAXBException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.opal.dto.legacy.CreditorAccount;
import uk.gov.hmcts.opal.util.XmlUtil;

class LegacyMinorCreditorSearchResultsResponseTest {

    @ParameterizedTest
    @ValueSource(strings = {"creditor_accounts_element", "creditorAccount"})
    void readsSearchResultsWithSupportedEntryNames(String entryName) throws JAXBException {
        LegacyMinorCreditorSearchResultsResponse response = XmlUtil.unmarshalXmlString("""
            <response>
              <count>2</count>
              <creditor_accounts>
                <%1$s>
                  <creditor_account_id>104</creditor_account_id>
                  <account_number>12345678A</account_number>
                  <account_balance>150.00</account_balance>
                  <defendant>
                    <organisation>false</organisation>
                    <firstnames>Jane</firstnames>
                    <surname>Smith</surname>
                  </defendant>
                </%1$s>
                <%1$s>
                  <creditor_account_id>105</creditor_account_id>
                  <account_number>12345678</account_number>
                  <account_balance>0.00</account_balance>
                </%1$s>
              </creditor_accounts>
            </response>
            """.formatted(entryName), LegacyMinorCreditorSearchResultsResponse.class);

        assertThat(response.getCount()).isEqualTo(2);
        assertThat(response.getCreditorAccounts()).extracting(CreditorAccount::getCreditorAccountId)
            .containsExactly("104", "105");
        CreditorAccount first = response.getCreditorAccounts().getFirst();
        assertThat(first.getAccountNumber()).isEqualTo("12345678A");
        assertThat(first.getAccountBalance()).isEqualTo(150.00);
        assertThat(first.getDefendant().getFirstnames()).isEqualTo("Jane");
        assertThat(first.getDefendant().getSurname()).isEqualTo("Smith");
    }
}
