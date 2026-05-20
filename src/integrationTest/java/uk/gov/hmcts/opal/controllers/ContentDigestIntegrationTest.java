package uk.gov.hmcts.opal.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Content-Digest default configuration integration tests")
class ContentDigestIntegrationTest extends AbstractContentDigestIntegrationTest {

    @Test
    void missingHeader_returnsSuccessWithoutResponseContentDigest() throws Exception {
        mockMvc.perform(get(ROOT_ENDPOINT))
            .andExpect(status().isOk())
            .andExpect(header().doesNotExist(CONTENT_DIGEST));
    }
}
