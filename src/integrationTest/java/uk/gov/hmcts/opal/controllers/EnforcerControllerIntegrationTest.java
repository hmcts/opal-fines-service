package uk.gov.hmcts.opal.controllers;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.clearInvocations;
import static uk.gov.hmcts.opal.support.SpyInvocationSupport.countInvocationsByMethodName;

import lombok.extern.slf4j.Slf4j;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.repository.EnforcerRepository;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@ActiveProfiles({"integration"})
@Slf4j(topic = "opal.EnforcerControllerIntegrationTest")
@Sql(scripts = "classpath:db/insertData/insert_into_enforcers.sql", executionPhase = BEFORE_TEST_CLASS)
@DisplayName("Enforcer Controller Integration Tests")
class EnforcerControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String URL_BASE = "/enforcers";

    @MockitoSpyBean
    private EnforcerRepository enforcerRepository;

    @Test
    @JiraStory("PO-304")
    @JiraStory("PO-316")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-5891")
    void testGetEnforcerById() throws Exception {
        ResultActions actions = mockMvc.perform(get(URL_BASE + "/1"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testGetEnforcerById: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.enforcerId").value(1L))
            .andExpect(jsonPath("$.enforcerCode").value(1))
            .andExpect(jsonPath("$.warrantReferenceSequence").value("101/09/00000"))
            .andExpect(jsonPath("$.warrantRegisterSequence").value(666))
            .andExpect(jsonPath("$.businessUnit.businessUnitId").value(5))
            .andExpect(jsonPath("$.name").value("AAA Enforcers"))
            .andExpect(jsonPath("$.addressLine1").value("9 Enforcement Street"))
            .andExpect(jsonPath("$.addressLine2").value("Enformentville"))
            .andExpect(jsonPath("$.addressLine3").value("Enforcementon"))
            .andExpect(jsonPath("$.postcode").value("EF1 1EF"))
            .andExpect(jsonPath("$.nameCy").value(IsNull.nullValue()))
            .andExpect(jsonPath("$.addressLine1Cy").value(IsNull.nullValue()))
            .andExpect(jsonPath("$.addressLine2Cy").value(IsNull.nullValue()))
            .andExpect(jsonPath("$.addressLine3Cy").value(IsNull.nullValue()));
    }

    @Test
    @JiraStory("PO-304")
    @JiraStory("PO-316")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-5889")
    void testGetEnforcerById_WhenEnforcerDoesNotExist() throws Exception {
        mockMvc.perform(get(URL_BASE + "/2"))
            .andExpect(status().isNotFound());
    }

    @Test
    @JiraStory("PO-304")
    @JiraStory("PO-316")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-5888")
    void testPostEnforcersSearch() throws Exception {
        ResultActions actions = mockMvc.perform(post(URL_BASE + "/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"aa\"}"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostEnforcersSearch: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].enforcerId").value(1L))
            .andExpect(jsonPath("$[0].enforcerCode").value(1))
            .andExpect(jsonPath("$[0].warrantReferenceSequence").value("101/09/00000"))
            .andExpect(jsonPath("$[0].warrantRegisterSequence").value(666))
            .andExpect(jsonPath("$[0].businessUnit.businessUnitId").value(5))
            .andExpect(jsonPath("$[0].name").value("AAA Enforcers"))
            .andExpect(jsonPath("$[0].addressLine1").value("9 Enforcement Street"))
            .andExpect(jsonPath("$[0].addressLine2").value("Enformentville"))
            .andExpect(jsonPath("$[0].addressLine3").value("Enforcementon"))
            .andExpect(jsonPath("$[0].postcode").value("EF1 1EF"))
            .andExpect(jsonPath("$[0].nameCy").value(IsNull.nullValue()))
            .andExpect(jsonPath("$[0].addressLine1Cy").value(IsNull.nullValue()))
            .andExpect(jsonPath("$[0].addressLine2Cy").value(IsNull.nullValue()))
            .andExpect(jsonPath("$[0].addressLine3Cy").value(IsNull.nullValue()));
    }

    @Test
    @JiraStory("PO-304")
    @JiraStory("PO-316")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-5890")
    void testPostEnforcersSearch_WhenEnforcerDoesNotExist() throws Exception {
        mockMvc.perform(post(URL_BASE + "/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"criteria\":\"2\"}"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Get Enforcer Ref Data [@PO-304, @PO-316]")
    @JiraStory("PO-304")
    @JiraStory("PO-316")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-5887")
    void testGetEnforcerRefData() throws Exception {
        ResultActions actions = mockMvc.perform(get(URL_BASE)
                                                    .header("authorization", userStateStub.getBearerToken()));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testGetEnforcerRefData: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(org.hamcrest.Matchers.anyOf(
                org.hamcrest.Matchers.is(83),
                org.hamcrest.Matchers.is(84)
            )))
            .andExpect(jsonPath("$.refData[?(@.enforcer_id == 1)].enforcer_code").value(hasItem(1)))
            .andExpect(jsonPath("$.refData[?(@.enforcer_id == 1)].name").value(hasItem("AAA Enforcers")));
    }

    @Test
    @DisplayName("Get Enforcer Ref Data uses cache on repeated identical request")
    @JiraStory("PO-7248")
    @JiraEpic("PO-8248")
    void testGetEnforcerRefData_usesCacheOnRepeatedRequest() throws Exception {
        clearInvocations(enforcerRepository);

        String firstBody = mockMvc.perform(get(URL_BASE)
                .header("authorization", userStateStub.getBearerToken()))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        String secondBody = mockMvc.perform(get(URL_BASE)
                .header("authorization", userStateStub.getBearerToken()))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertEquals(firstBody, secondBody);
        assertEquals(1, countInvocationsByMethodName(enforcerRepository, "findBy"));
    }

}
