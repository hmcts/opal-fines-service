package uk.gov.hmcts.opal.dto;


import java.time.LocalDate;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountSearchDto implements ToJsonString {
    /** Defendant Surname, Company Name or A/C number. */
    private String nameOrAccountNo;
    /** Can be either Defendant, Minor Creditor or Company. */
    private String searchType;
    /** Defendant Forenames. */
    private String forename;
    /** Defendant Initials. */
    private String initials;
    /** Defendant Date of Birth. */
    private LocalDate dateOfBirth;
    /** Defendant Address, typically just first line. */
    private String address;
    /** National Insurance Number. */
    private String niNumber;
    /** Prosecutor Case Reference. */
    private String pcr;
    /** Major Creditor account selection. */
    private String majorCreditor;
    /** Unsure. */
    private String tillNumber;
}
