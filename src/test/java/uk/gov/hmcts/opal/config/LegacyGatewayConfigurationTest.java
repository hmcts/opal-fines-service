package uk.gov.hmcts.opal.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.common.legacy.client.LegacyGatewayClient;
import uk.gov.hmcts.opal.common.legacy.config.LegacyGatewayProperties;
import uk.gov.hmcts.opal.common.legacy.service.GatewayService;

class LegacyGatewayConfigurationTest {

    @Test
    void gatewayService_usesFeignClient() {
        LegacyGatewayProperties properties = new LegacyGatewayProperties();
        properties.setUrl("http://localhost/opal");
        properties.setUsername("user");
        properties.setPassword("password");
        LegacyGatewayClient client = mock(LegacyGatewayClient.class);
        when(client.post("getAccount", "Basic dXNlcjpwYXNzd29yZA==", "{}"))
            .thenReturn(ResponseEntity.ok("legacy response"));

        GatewayService gatewayService = new LegacyGatewayConfiguration().gatewayService(properties, client);
        GatewayService.Response<String> response = gatewayService.postToGateway("getAccount", String.class, "{}", null);

        assertThat(response.code).isEqualTo(HttpStatus.OK);
        assertThat(response.body).isEqualTo("legacy response");
    }
}
