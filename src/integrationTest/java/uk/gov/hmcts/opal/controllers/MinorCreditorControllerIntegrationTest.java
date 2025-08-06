package uk.gov.hmcts.opal.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorEntity;

import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ActiveProfiles({"integration"})
@Slf4j(topic = "opal.MinorCreditorControllerIntegrationTest")
//@Sql(scripts = "classpath:db/insertData/insert_into_creditor_accounts.sql", executionPhase = BEFORE_TEST_CLASS)
@DisplayName("MinorCreditorController Integration Test")
public class MinorCreditorControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String URL_BASE = "/minor-creditor-accounts/";


    @Test
    @DisplayName("tests run as expected")
    void postMinorCreditorsSearch_shouldReturnOk() throws Exception {
        MinorCreditorEntity criteria = new MinorCreditorEntity();
        criteria.setBusinessUnitId("1");
        criteria.setName("test");
        criteria.setPostcode("CR1 1CR");
        criteria.setAddressLine1("123 sesame street");
        criteria.setAddressLine2("Creditville");
        criteria.setAddressLine3("Crediton");

        ResultActions actions = mockMvc.perform(post(URL_BASE + "search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(criteria)));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostMinorCreditorsSearch: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].minorCreditorId").value("1"))
            .andExpect(jsonPath("$[0].name").value("test"))
            .andExpect(jsonPath("$[0].addressLine1").value("Credit Lane"))
            .andExpect(jsonPath("$[0].addressLine2").value("Creditville"))
            .andExpect(jsonPath("$[0].addressLine3").value("Crediton"))
            .andExpect(jsonPath("$[0].postcode").value("CR1 1CR"));
    }
}
