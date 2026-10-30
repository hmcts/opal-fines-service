package uk.gov.hmcts.opal.controllers.shared.util;

import java.util.List;
import uk.gov.hmcts.opal.generated.model.DefendantAccountSearchReferenceNumberDefendantAccount;
import uk.gov.hmcts.opal.generated.model.PostDefendantAccountSearchRequestDefendantAccount;

public final class DefendantSearchRequestUtil {

    private DefendantSearchRequestUtil() {
    }

    public static PostDefendantAccountSearchRequestDefendantAccount searchRequest(String accountNumber,
        boolean organisation, Boolean consolidationSearch) {

        return PostDefendantAccountSearchRequestDefendantAccount.builder()
            .activeAccountsOnly(true)
            .businessUnitIds(List.of((short) 78))
            .referenceNumber(new DefendantAccountSearchReferenceNumberDefendantAccount()
                .organisation(organisation).accountNumber(accountNumber))
            .consolidationSearch(consolidationSearch)
            .build();
    }
}
