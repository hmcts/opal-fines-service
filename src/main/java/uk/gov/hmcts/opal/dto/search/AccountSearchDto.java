package uk.gov.hmcts.opal.dto.search;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("search_type")
    private String searchType;
    /** Defendant Forenames. */
    private String forename;
    /** Defendant Initials. */
    private String initials;
    /** Defendant Date of Birth. */
    @JsonProperty("date_of_birth")
    private DateDto dateOfBirth;
    /** Defendant Address. */
    @JsonProperty("address_line")
    private String addressLine;
    /** Defendant Postcode. */
    private String postcode;
    /** National Insurance Number. */
    @JsonProperty("ni_number")
    private String niNumber;
    /** Prosecutor Case Reference. */
    private String pcr;
    /** Major Creditor account selection. */
    @JsonProperty("major_creditor")
    private String majorCreditor;
    /** Unsure. */
    @JsonProperty("till_number")
    private String tillNumber;

    @JsonIgnore
    public Optional<Long> getNumericCourt() {
        return Optional.ofNullable(getCourt())
            .filter(s -> s.matches("[0-9]+"))
            .map(Long::parseLong);
    }
}
