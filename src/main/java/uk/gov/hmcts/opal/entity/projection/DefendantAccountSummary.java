package uk.gov.hmcts.opal.entity.projection;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.opal.entity.FullNameBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

public interface DefendantAccountSummary {

    /** The defendant account id used as the primary key in the database. */
    @JsonProperty("defendant_account_id")
    Long getDefendantAccountId();

    /** The defendant account number. */
    @JsonProperty("account_number")
    String getAccountNumber();

    /** The balance on the defendant account. */
    @JsonProperty("account_balance")
    BigDecimal getAccountBalance();

    /** The Court (traditionally abbreviated to CT) 'Accounting Division' or 'Metropolitan Area' (MET). */
    @JsonProperty("imposing_court_id")
    String getImposingCourtId();

    @JsonProperty("parties")
    Set<PartyLink> getParties();

    interface PartyLink {
        @JsonProperty("party")
        PartyDefendantAccountSummary getParty();
    }

    interface PartyDefendantAccountSummary extends FullNameBuilder {

        /** The date of birth of the defendant. */
        @JsonProperty("date_of_birth")
        LocalDate getDateOfBirth();

        /** First line of the defendant's address. */
        @JsonProperty("address_line_1")
        String getAddressLine1();

    }
}
