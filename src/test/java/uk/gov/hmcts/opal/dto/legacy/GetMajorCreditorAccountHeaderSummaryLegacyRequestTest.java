package uk.gov.hmcts.opal.dto.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class GetMajorCreditorAccountHeaderSummaryLegacyRequestTest {

    @Test
    void serializesCreditorAccountIdUsingLegacyFieldName() throws Exception {
        GetMajorCreditorAccountHeaderSummaryLegacyRequest request =
            GetMajorCreditorAccountHeaderSummaryLegacyRequest.builder()
                .creditorAccountId("99000000000800")
                .build();

        assertEquals("{\"creditor_account_id\":\"99000000000800\"}", new ObjectMapper().writeValueAsString(request));
    }
}
