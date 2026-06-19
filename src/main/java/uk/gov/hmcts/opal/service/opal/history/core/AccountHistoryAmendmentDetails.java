package uk.gov.hmcts.opal.service.opal.history.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountHistoryAmendmentDetails implements AccountHistoryDetails {

    private String attributeName;

    private String oldValue;

    private String newValue;
}
