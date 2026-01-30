package uk.gov.hmcts.opal.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DefendantDto {
    private String defendantAccountId;
    private boolean organisation;
    private String organisationName;
    private String firstnames;
    private String surname;

    @JsonProperty("account_number")
    private String accountNumber;

    @JsonProperty("account_id")
    private Long accountId;
}
