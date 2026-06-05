package uk.gov.hmcts.opal.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigInteger;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.opal.generated.model.GetMajorCreditorAccountHeaderSummary200Response;
import uk.gov.hmcts.opal.util.Versioned;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetMajorCreditorAccountHeaderSummaryResponse
    extends GetMajorCreditorAccountHeaderSummary200Response implements ToJsonString, Versioned {

    @JsonIgnore
    private BigInteger version;
}
