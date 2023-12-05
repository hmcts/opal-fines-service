package uk.gov.hmcts.opal.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface DefendantAccountSummary {

    /** The defendant account number. */
    String getAccountNumber();

    /** The name of the defendant, being an aggregation of surname, firstnames and title. */
    String getOriginatorName();

    /** The date of birth of the defendant. */
    LocalDate getDateOfBirth();

    /** First line of the defendant's address. */
    String getAddressLine1();

    /** The balance on the defendant account. */
    BigDecimal getAccountBalance();

    /** The Court (traditionally abbreviated to CT) 'Accounting Division' or 'Metropolitan Area' (MET). */
    String getImposingCourtId();

}
