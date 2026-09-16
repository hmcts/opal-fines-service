package uk.gov.hmcts.opal.dto.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigInteger;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

class AddPaymentCardLegacyRequestTest {

    @Test
    void shouldSerializeUsingSnakeCasePropertyNames() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        AddPaymentCardLegacyRequest request = AddPaymentCardLegacyRequest.builder()
            .defendantAccountId("770000004141")
            .businessUnitId("77")
            .businessUnitUserId("L077JG")
            .version(new BigInteger("835509468493002959816526022013198014020000027379"))
            .build();

        String json = objectMapper.writeValueAsString(request);
        JsonNode payload = objectMapper.readTree(json);

        assertEquals("770000004141", payload.get("defendant_account_id").asString());
        assertEquals("77", payload.get("business_unit_id").asString());
        assertEquals("L077JG", payload.get("business_unit_user_id").asString());
        assertEquals(new BigInteger("835509468493002959816526022013198014020000027379"),
                     payload.get("version").bigIntegerValue());
        assertNull(payload.get("businessUnitUserId"));
    }
}
