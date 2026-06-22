package uk.gov.hmcts.opal.dto.history;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AmendmentDetails implements DefendantAccountHistoryDetails {

    private String attributeName;

    private String oldValue;

    private String newValue;
}
