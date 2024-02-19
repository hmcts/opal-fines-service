package uk.gov.hmcts.opal.entity.projection;

import uk.gov.hmcts.opal.entity.FullNameBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

public interface DefendantAccountSummary {

    /** The defendant account id used as the primary key in the database. */
    Long getDefendantAccountId();

    /** The defendant account number. */
    String getAccountNumber();

    /** The balance on the defendant account. */
    BigDecimal getAccountBalance();

    /** The Court (traditionally abbreviated to CT) 'Accounting Division' or 'Metropolitan Area' (MET). */
    String getImposingCourtId();

    Set<PartyLink> getParties();

    interface PartyLink {
        PartyDefendantAccountSummary getParty();
    }

    interface PartyDefendantAccountSummary extends FullNameBuilder {

        /** The date of birth of the defendant. */
        LocalDate getDateOfBirth();

        /** First line of the defendant's address. */
        String getAddressLine1();

    }
}
