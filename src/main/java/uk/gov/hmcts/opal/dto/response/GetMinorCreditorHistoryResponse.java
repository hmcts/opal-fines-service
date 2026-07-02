package uk.gov.hmcts.opal.dto.response;

import java.math.BigInteger;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.generated.model.GetMinorCreditorHistory200Response;
import uk.gov.hmcts.opal.util.Versioned;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetMinorCreditorHistoryResponse implements Versioned {

    private GetMinorCreditorHistory200Response payload;

    private BigInteger version;
}
