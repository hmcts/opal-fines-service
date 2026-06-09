package uk.gov.hmcts.opal.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigInteger;
import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.opal.generated.model.GetCentralFundResponse;
import uk.gov.hmcts.opal.util.Versioned;

@Builder
@Getter
public class CentralFundResponse implements Versioned {

    private GetCentralFundResponse payload;

    @JsonIgnore
    private BigInteger version;
}
