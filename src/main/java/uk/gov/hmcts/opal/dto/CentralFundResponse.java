package uk.gov.hmcts.opal.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigInteger;
import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.opal.generated.model.GetCentralFundByBusinessUnit200Response;
import uk.gov.hmcts.opal.util.Versioned;

@Builder
@Getter
public class CentralFundResponse implements Versioned {

    private GetCentralFundByBusinessUnit200Response payload;

    @JsonIgnore
    private BigInteger version;
}
