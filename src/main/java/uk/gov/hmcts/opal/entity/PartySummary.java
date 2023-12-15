package uk.gov.hmcts.opal.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

public interface PartySummary {

    String getTitle();

    String getForenames();

    String getSurname();

    /**
     * The name of the defendant, being an aggregation of surname, firstnames and title.
     */
    default String getName() {
        return getTitle().concat(" ").concat(getForenames()).concat(" ").concat(getSurname());
    }

    /**
     * The date of birth of the defendant.
     */
    LocalDate getDateOfBirth();

    /**
     * First line of the defendant's address.
     */
    String getAddressLine1();

    Set<DefendantAccountLink> getDefendantAccounts();

    interface DefendantAccountLink {
        DefendantAccountPartySummary getDefendantAccount();
    }

    interface DefendantAccountPartySummary {

        /**
         * The defendant account number.
         */
        String getAccountNumber();

        /**
         * The balance on the defendant account.
         */
        BigDecimal getAccountBalance();

        /**
         * The Court (traditionally abbreviated to CT) 'Accounting Division' or 'Metropolitan Area' (MET).
         */
        String getImposingCourtId();

    }
}
