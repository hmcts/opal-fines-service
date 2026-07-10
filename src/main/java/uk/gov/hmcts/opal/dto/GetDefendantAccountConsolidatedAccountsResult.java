package uk.gov.hmcts.opal.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigInteger;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.generated.model.ConsolidatedAccountDefendantAccount;
import uk.gov.hmcts.opal.util.Versioned;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class GetDefendantAccountConsolidatedAccountsResult implements Versioned {

    private List<ConsolidatedAccountDefendantAccount> payload;

    private BigInteger version;
}
