package uk.gov.hmcts.opal.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigInteger;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.generated.model.DefendantAccountAtAGlanceResponseDefendantAccount;
import uk.gov.hmcts.opal.util.Versioned;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class GetDefendantAccountAtAGlanceResponse implements Versioned {

    private DefendantAccountAtAGlanceResponseDefendantAccount payload;

    private BigInteger version;
}
