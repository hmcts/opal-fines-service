package uk.gov.hmcts.opal.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
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

    public interface PartyDefendantAccountSummary {

        String getTitle();

        String getForenames();

        String getSurname();

        /** The name of the defendant, being an aggregation of surname, firstnames and title. */
        default String getName() {
            String title = Optional.ofNullable(getTitle()).map(s -> s.concat(" ")).orElse("");
            String forenames = Optional.ofNullable(getForenames()).map(s -> s.concat(" ")).orElse("");
            String surname = Optional.ofNullable(getSurname()).orElse("");
            return title.concat(forenames).concat(surname);
        }

        /** The date of birth of the defendant. */
        LocalDate getDateOfBirth();

        /** First line of the defendant's address. */
        String getAddressLine1();

    }
}
