package uk.gov.hmcts.opal.controllers;

import java.io.UnsupportedEncodingException;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.SchemaPaths;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles({"integration"})
@Slf4j(topic = "opal.LocalJusticeAreaControllerIntegrationTest")
@Sql(scripts = "classpath:db/insertData/insert_into_local_justice_area.sql", executionPhase = BEFORE_TEST_CLASS)
@DisplayName("LocalJusticeAreaController Integration Test")
class LocalJusticeAreaControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String URL_BASE = "/local-justice-areas";
    private static final String GET_LJAS_REF_DATA_RESPONSE =
        SchemaPaths.REFERENCE_DATA + "/getLJARefDataResponse.json";

    @MockitoSpyBean
    private JsonSchemaValidationService jsonSchemaValidationService;

    private static Stream<Arguments> testCasesForQueryParameterInput() {
        return Stream.of(
            Arguments.of(get(URL_BASE).param("lja_type", "TYPE_1", "TYPE_2")),
            Arguments.of(get(URL_BASE).param("lja_type", "TYPE_1")
                .param("lja_type", "TYPE_2")),
            Arguments.of(get(URL_BASE + "?lja_type=TYPE_1,TYPE_2"))
        );
    }

    @Test
    @DisplayName("Get local justice area by ID [@PO-312, PO-304]")
    void testGetLocalJusticeAreaById() throws Exception {

        ResultActions actions = mockMvc.perform(get(URL_BASE + "/1"));

        getResponseBody(actions, ":testGetLocalJusticeAreaById:");

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.localJusticeAreaId").value(1))
            .andExpect(jsonPath("$.name").value("AAAA Trial Court"))
            .andExpect(jsonPath("$.addressLine1").value("Alpha Trial Courts"))
            .andExpect(jsonPath("$.addressLine2").value("Court Quarter"))
            .andExpect(jsonPath("$.addressLine3").value("666 Trial Street"))
            .andExpect(jsonPath("$.postcode").value("TR12 1TR"));
    }


    @Test
    @DisplayName("No local justice area returned when local justice area does not exist [@PO-312, PO-304]")
    void testGetLocalJusticeAreaById_WhenLocalJusticeAreaDoesNotExist() throws Exception {

        mockMvc.perform(get(URL_BASE + "/2"))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Verify search result for local justice area created by POST request [@PO-312, PO-304]")
    void testPostLocalJusticeAreasSearch() throws Exception {

        ResultActions actions = mockMvc.perform(post(URL_BASE + "/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"ljaCode\":\"00\"}"));

        getResponseBody(actions, ":testPostLocalJusticeAreasSearch:");

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].localJusticeAreaId").value(1))
            .andExpect(jsonPath("$[0].name").value("AAAA Trial Court"))
            .andExpect(jsonPath("$[0].addressLine1").value("Alpha Trial Courts"))
            .andExpect(jsonPath("$[0].addressLine2").value("Court Quarter"))
            .andExpect(jsonPath("$[0].addressLine3").value("666 Trial Street"))
            .andExpect(jsonPath("$[0].postcode").value("TR12 1TR"));
    }

    @Test
    @DisplayName("Verify no search result when local justice area does not exist [@PO-312, PO-304]")
    void testPostLocalJusticeAreasSearch_WhenLocalJusticeAreaDoesNotExist() throws Exception {
        mockMvc.perform(post(URL_BASE + "/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"criteria\":\"2\"}"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Verify search result for LocalJusticeAreasRefData created by GET request no filters [@PO-2757]")
    public void testGetLocalJusticeAreasRefData_returnAllData() throws Exception {
        var actions = mockMvc.perform(get(URL_BASE));

        String body = getResponseBody(actions, ":testGetLocalJusticeAreasRefData:returnAllData:");

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            // at least one item present
            .andExpect(jsonPath("$.count").isNumber())
            .andExpect(jsonPath("$.count").value(org.hamcrest.Matchers.greaterThan(1)))
            // verify the seeded LJA exists somewhere in the array (don’t rely on index 0)
            .andExpect(jsonPath("$.refData[?(@.name == 'AAAA Trial Court')].local_justice_area_id",
                org.hamcrest.Matchers.hasItem(1)))
            .andExpect(jsonPath("$.refData[?(@.local_justice_area_id == 1)].address_line_1",
                org.hamcrest.Matchers.hasItem("Alpha Trial Courts")))
            .andExpect(jsonPath("$.refData[?(@.local_justice_area_id == 1)].lja_code",
                org.hamcrest.Matchers.hasItem("0007")))
            .andExpect(jsonPath("$.refData[?(@.local_justice_area_id == 1)].lja_type",
                org.hamcrest.Matchers.hasItem("TYPE_1")))
            .andExpect(jsonPath("$.refData[?(@.local_justice_area_id == 10)].lja_type",
                org.hamcrest.Matchers.hasItem("TYPE_2")));

        jsonSchemaValidationService.validateOrError(body, GET_LJAS_REF_DATA_RESPONSE);
    }

    @Test
    @DisplayName("Verify search result for LocalJusticeAreasRefData created by GET request with single lja_type param [@PO-2757]")
    public void testGetLocalJusticeAreasRefData_filterBySingleLjaType() throws Exception {
        var actions = mockMvc.perform(get(URL_BASE).param("lja_type", "TYPE_1"));

        String body = getResponseBody(actions, ":testGetLocalJusticeAreasRefData:filterBySingleLjaType");

        actions.andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.refData[?(@.local_justice_area_id == 1)].lja_type",
                org.hamcrest.Matchers.hasItem("TYPE_1")));

        jsonSchemaValidationService.validateOrError(body, GET_LJAS_REF_DATA_RESPONSE);
    }

    @Test
    @DisplayName("Verify search result for LocalJusticeAreasRefData created by GET request with unknown lja_type param [@PO-2757]")
    public void testGetLocalJusticeAreasRefData_filterByUnknownLjaType() throws Exception {
        var actions = mockMvc.perform(get(URL_BASE).param("lja_type", "UNKNOWN"));

        String body = getResponseBody(actions, ":testGetLocalJusticeAreasRefData:filterByUnknownLjaType:");

        actions.andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(0));

        jsonSchemaValidationService.validateOrError(body, GET_LJAS_REF_DATA_RESPONSE);
    }

    @Test
    @DisplayName("Verify search result for LocalJusticeAreasRefData created by GET request with known and unknown lja_type param [@PO-2757]")
    public void testGetLocalJusticeAreasRefData_filterByKnownAndUnknownLjaTypes() throws Exception {
        var actions = mockMvc.perform(get(URL_BASE).param("lja_type", "UNKNOWN", "TYPE_1"));
        String body = getResponseBody(actions, ":testGetLocalJusticeAreasRefData:filterByKnownAndUnknownLjaTypes:");

        actions.andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.refData[?(@.local_justice_area_id == 1)].lja_type",
                org.hamcrest.Matchers.hasItem("TYPE_1")));

        jsonSchemaValidationService.validateOrError(body, GET_LJAS_REF_DATA_RESPONSE);
    }

    @ParameterizedTest
    @MethodSource("testCasesForQueryParameterInput")
    @DisplayName("Verify search result for LocalJusticeAreasRefData created by GET request with single lja_type param [@PO-2757]")
    public void testGetLocalJusticeAreasRefData_filterByMultipleLjaTypes(MockHttpServletRequestBuilder requestBuilder)
        throws Exception {
        ResultActions actions = mockMvc.perform(requestBuilder);
        String body = getResponseBody(actions, ":testGetLocalJusticeAreasRefData:filterByMultipleLjaTypes:");

        actions.andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(org.hamcrest.Matchers.greaterThan(2)))
            .andExpect(jsonPath("$.refData[?(@.local_justice_area_id == 1)].lja_type",
                org.hamcrest.Matchers.hasItem("TYPE_1")))
            .andExpect(jsonPath("$.refData[?(@.local_justice_area_id == 10)].lja_type",
                org.hamcrest.Matchers.hasItem("TYPE_2")));

        jsonSchemaValidationService.validateOrError(body, GET_LJAS_REF_DATA_RESPONSE);

    }

    @Test
    @DisplayName("Verify deterministic results for LocalJusticeAreasRefData created by multiple GET requests")
    void testGetLocalJusticeAreasRefData_returnsSameResultsInStableOrderForMultipleCalls() throws Exception {

        var actions1 = mockMvc.perform(get(URL_BASE).param("lja_type", "CTYCRT", "TYPE_2"));
        String body1 = actions1.andReturn().getResponse().getContentAsString();

        var actions2 = mockMvc.perform(get(URL_BASE).param("lja_type", "TYPE_2", "CTYCRT"));
        String body2 = actions2.andReturn().getResponse().getContentAsString();

        assertEquals(body1, body2);
    }

    @Test
    @DisplayName("Verify search result for LocalJusticeAreasRefData created by GET request with filter query params")
    public void testGetLocalJusticeAreasRefData_whenAllQueryParamsPresent() throws Exception {
        var actions = mockMvc.perform(get(URL_BASE)
            .param("q", "0007")
            .param("lja_type", "TYPE_1", "NOT_VALID"));
        getResponseBody(actions, ":testGetLocalJusticeAreasRefData:whenAllQueryParamsPresent");

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            // at least one item present
            .andExpect(jsonPath("$.count").isNumber())
            .andExpect(jsonPath("$.refData[?(@.local_justice_area_id == 1)].lja_code",
                org.hamcrest.Matchers.hasItem("0007")))
            .andExpect(jsonPath("$.refData[?(@.local_justice_area_id == 1)].lja_type",
                org.hamcrest.Matchers.hasItem("TYPE_1")));
    }

    private @NonNull String getResponseBody(ResultActions actions, String methodName)
        throws UnsupportedEncodingException {
        String body = actions.andReturn().getResponse().getContentAsString();
        log.info("{} Response body: /n{}", methodName, ToJsonString.toPrettyJson(body));
        return body;
    }
}
