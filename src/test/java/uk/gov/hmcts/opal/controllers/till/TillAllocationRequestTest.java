package uk.gov.hmcts.opal.controllers.till;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.service.opal.till.TillAllocationService;

class TillAllocationRequestTest {

    private TillAllocationService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = mock(TillAllocationService.class);
        TillsApiController controller = new TillsApiController(mock(DynamicConfigService.class), null, null, null,
            service);
        mockMvc = standaloneSetup(controller).build();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "{}", "{\"tills\":null}", "{\"tills\":[]}", "{\"tills\":[{}]}",
        "{\"tills\":[{\"till_id\":1}]}", "{\"tills\":[{\"business_unit_id\":78}]}",
        "{\"tills\":[{\"till_id\":\"not-a-number\",\"business_unit_id\":78}]}", "{"})
    void rejectsInvalidRequest(String body) throws Exception {
        mockMvc.perform(post("/tills/allocate").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }
}
