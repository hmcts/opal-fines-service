package uk.gov.hmcts.opal.controllers.shared;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.opal.common.user.authentication.exception.CommonGlobalExceptionHandler;
import uk.gov.hmcts.opal.common.controllers.advice.OpalGlobalExceptionHandler;
import uk.gov.hmcts.opal.controllers.AbstractWebMvcIntegrationTest;
import uk.gov.hmcts.opal.controllers.RootController;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@ActiveProfiles({"integration"})
@WebMvcTest(
    controllers = RootController.class,
    excludeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE, classes = CommonGlobalExceptionHandler.class)
)
@Import({RootController.class, JsonSchemaValidationService.class, OpalGlobalExceptionHandler.class})
@Slf4j(topic = "opal.GetWelcomeTest")
@DisplayName("Get Welcome Test Integration Tests")
class GetWelcomeTest extends AbstractWebMvcIntegrationTest {

    @Test
    @DisplayName("Should welcome upon root request with 200 response code")
    @JiraStory("PO-125")
    @JiraEpic("PO-2233")
    @JiraTestKey("PO-5892")
    void welcomeRootEndpoint() throws Exception {
        log.info(":welcomeRootEndpoint:");
        MvcResult response = mockMvc.perform(get("/"))
            .andExpect(status().isOk())
            .andReturn();

        assertThat(response.getResponse().getContentAsString()).startsWith("Welcome");
    }
}
