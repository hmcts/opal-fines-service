package uk.gov.hmcts.opal.dto.search;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.opal.dto.DateDto;
import uk.gov.hmcts.opal.dto.ToJsonString;

import java.util.Optional;

@Data
@Builder
public class AccountSearchDto implements ToJsonString {
    /** The court (CT) or MET (metropolitan area). */
    private String court;
    /** Defendant Surname, Company Name or A/C number. */
    private String surname;
    /** Can be either Defendant, Minor Creditor or Company. */
    private String searchType;
    /** Defendant Forenames. */
    private String forename;
    /** Defendant Initials. */
    private String initials;
    /** Defendant Date of Birth. */
    private DateDto dateOfBirth;
    /** Defendant Address, typically just first line. */
    private String addressLineOne;
    /** National Insurance Number. */
    private String niNumber;
    /** Prosecutor Case Reference. */
    private String pcr;
    /** Major Creditor account selection. */
    private String majorCreditor;
    /** Unsure. */
    private String tillNumber;

    @JsonIgnore
    public Optional<Long> getNumericCourt() {
        return Optional.ofNullable(getCourt())
            .filter(s -> s.matches("[0-9]+"))
            .map(Long::parseLong);
    }
}
