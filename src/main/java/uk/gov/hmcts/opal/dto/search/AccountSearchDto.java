package uk.gov.hmcts.opal.dto.search;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.opal.dto.ToJsonString;

import java.util.Optional;

@Data
@Builder
public class AccountSearchDto implements ToJsonString {
    /** The court (CT) or MET (metropolitan area). */
    private String court;
    /** Defendant Surname, Company Name or A/C number. */
    @JsonProperty("search_type")
    private String searchType;
    private String pcr;
    /** Major Creditor account selection. */
    @JsonProperty("major_creditor")
    private String majorCreditor;
    /** Unsure. */
    @JsonProperty("till_number")
    private String tillNumber;

    private DefendantDto defendant;

    @JsonIgnore
    public Optional<Long> getNumericCourt() {
        return Optional.ofNullable(getCourt())
            .filter(s -> s.matches("[0-9]+"))
            .map(Long::parseLong);
    }
}
