package uk.gov.hmcts.opal.dto;


import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountSummaryDto {
    /** The defendant account number. */
    private String accountNo;
    /** The name of the defendant, being an aggregation of surname, firstnames and title. */
    private String name;
    /** The date of birth of the defendant. */
    private LocalDate dateOfBirth;
    /** First line of the defendant's address. */
    private String addressLine1;
    /** The balance on the defendant account. */
    private BigDecimal balance;
    /** The Court (traditionally abbreviated to CT) 'Accounting Division' or 'Metropolitan Area' (MET). */
    private String court;
}
