package uk.gov.hmcts.opal.dto.reference;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.ToJsonString;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "major_creditor_id")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MajorCreditorReferenceData implements ToJsonString {

    @JsonProperty("major_creditor_id")
    private Long majorCreditorId;

    @JsonProperty("business_unit_id")
    private Short businessUnitId;

    @JsonProperty("major_creditor_code")
    private String majorCreditorCode;

    @JsonProperty("name")
    private String name;

    @JsonProperty("postcode")
    private String postcode;

    @JsonProperty("creditor_account_id")
    private Long creditorAccountId;

    @JsonProperty("account_number")
    private String accountNumber;

    @JsonProperty("creditor_account_type")
    private String creditorAccountType;

    @JsonProperty("prosecution_service")
    private Boolean prosecutionService;

    @JsonProperty("minor_creditor_party_id")
    private Long minorCreditorPartyId;

    @JsonProperty("from_suspense")
    private Boolean fromSuspense;

    @JsonProperty("hold_payout")
    private Boolean holdPayout;

    @JsonProperty("last_changed_date")
    private LocalDateTime lastChangedDate;

}
