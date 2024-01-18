package uk.gov.hmcts.opal.dto.legacy;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.opal.dto.ToJsonString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
public class LegacyAccountDetailsRequestDto implements ToJsonString {

    //defendant_accounts.defendant_account_id
    @JsonProperty("defendant_account_id")
    private Long defendantAccountId;

}
