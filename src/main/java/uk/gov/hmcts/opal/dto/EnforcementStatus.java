package uk.gov.hmcts.opal.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigInteger;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.opal.generated.model.GetDefendantAccountEnforcementStatusResponse;
import uk.gov.hmcts.opal.util.Versioned;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL) // This line forces the HTTP Response to be of type 'application/json'
public class EnforcementStatus extends GetDefendantAccountEnforcementStatusResponse implements ToJsonString, Versioned {

    @JsonIgnore
    public BigInteger version;

}
