package uk.gov.hmcts.opal.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
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
    @JsonProperty("defendant_account_id")
    private String defendantAccountId;

    @JsonProperty("organisation")
    private boolean organisation;

    @JsonProperty("organisation_name")
    private String organisationName;

    @JsonProperty("firstnames")
    private String firstnames;

    @JsonProperty("surname")
    private String surname;
}
