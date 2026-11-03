package uk.gov.hmcts.opal.controllers.r1a;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.clearInvocations;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.support.SpyInvocationSupport.countInvocationsByMethodName;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.SchemaPaths;
import uk.gov.hmcts.opal.repository.LocalJusticeAreaLegacyRepository;
import uk.gov.hmcts.opal.repository.LocalJusticeAreaRepository;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@ActiveProfiles({"integration", "opal"})
@Sql(scripts = "classpath:db/insertData/insert_into_local_justice_area.sql", executionPhase = BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:db/deleteData/delete_from_local_justice_area.sql", executionPhase = AFTER_TEST_CLASS)
@DisplayName("Opal Local Justice Area Controller Integration Test")
@Isolated
@Tag("ExtendedTest")
class OpalLocalJusticeAreaControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String URL_BASE = "/local-justice-areas";
    private static final String LJA_TYPE_PARAM = "lja_type";
    private static final String GET_LJAS_REF_DATA_RESPONSE =
        SchemaPaths.REFERENCE_DATA + "/getLJARefDataResponse.json";

    @MockitoSpyBean
    private CacheManager cacheManager;

    @MockitoSpyBean
    private LocalJusticeAreaRepository localJusticeAreaRepository;

    @MockitoSpyBean
    private LocalJusticeAreaLegacyRepository localJusticeAreaLegacyRepository;

    @MockitoSpyBean
    private JsonSchemaValidationService jsonSchemaValidationService;

    private static Stream<Arguments> multipleLjaTypeRequests() {
        return Stream.of(
            Arguments.of(get(URL_BASE).param(LJA_TYPE_PARAM, "LJA", "SJCRT")),
            Arguments.of(get(URL_BASE).param(LJA_TYPE_PARAM, "LJA").param(LJA_TYPE_PARAM, "SJCRT")),
            Arguments.of(get(URL_BASE + "?lja_type=LJA,SJCRT"))
        );
    }

    @Test
    @JiraStory("PO-312")
    @JiraStory("PO-304")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-5963")
    void getLocalJusticeAreaById_returnsLocalJusticeAreaFromOpalTable() throws Exception {
        clearRepositoryInvocations();

        mockMvc.perform(get(URL_BASE + "/1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.localJusticeAreaId").value(1))
            .andExpect(jsonPath("$.name").value("AAAA Trial Court"))
            .andExpect(jsonPath("$.addressLine1").value("Alpha Trial Courts"))
            .andExpect(jsonPath("$.addressLine2").value("Court Quarter"))
            .andExpect(jsonPath("$.addressLine3").value("666 Trial Street"))
            .andExpect(jsonPath("$.postcode").value("TR12 1TR"));

        assertOnlyOpalRepositoryUsed("findById");
    }

    @Test
    @JiraStory("PO-312")
    @JiraStory("PO-304")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-5966")
    void getLocalJusticeAreaById_returnsNotFound() throws Exception {
        clearRepositoryInvocations();

        mockMvc.perform(get(URL_BASE + "/2"))
            .andExpect(status().isNotFound());

        assertOnlyOpalRepositoryUsed("findById");
    }

    @Test
    @JiraStory("PO-2757")
    @JiraEpic("PO-2750")
    @JiraTestKey("PO-5960")
    void getLocalJusticeAreaRefData_returnsAllDataFromOpalTable() throws Exception {
        clearRepositoryInvocations();

        ResultActions actions = mockMvc.perform(get(URL_BASE));
        String body = actions.andReturn().getResponse().getContentAsString();

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(greaterThan(1)))
            .andExpect(jsonPath("$.refData[?(@.name == 'AAAA Trial Court')].local_justice_area_id", hasItem(1)))
            .andExpect(jsonPath("$.refData[?(@.local_justice_area_id == 1)].address_line_1",
                hasItem("Alpha Trial Courts")))
            .andExpect(jsonPath("$.refData[?(@.local_justice_area_id == 1)].lja_type", hasItem("LJA")))
            .andExpect(jsonPath("$.refData[?(@.local_justice_area_id == 10)].lja_type", hasItem("SJCRT")));

        jsonSchemaValidationService.validateOrError(body, GET_LJAS_REF_DATA_RESPONSE);
        assertOnlyOpalRepositoryUsed("findBy");
    }

    @Test
    @JiraStory("PO-2757")
    @JiraEpic("PO-2750")
    @JiraTestKey("PO-5965")
    void getLocalJusticeAreaRefData_filtersBySingleType() throws Exception {
        clearRepositoryInvocations();

        ResultActions actions = mockMvc.perform(get(URL_BASE).param(LJA_TYPE_PARAM, "LJA"));
        String body = actions.andReturn().getResponse().getContentAsString();

        actions.andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(greaterThan(0)))
            .andExpect(jsonPath("$.refData[?(@.local_justice_area_id == 1)].lja_type", hasItem("LJA")));

        jsonSchemaValidationService.validateOrError(body, GET_LJAS_REF_DATA_RESPONSE);
        assertOnlyOpalRepositoryUsed("findBy");
    }

    @Test
    @JiraStory("PO-2757")
    @JiraEpic("PO-2750")
    @JiraTestKey("PO-5970")
    void getLocalJusticeAreaRefData_rejectsUnknownType() throws Exception {
        mockMvc.perform(get(URL_BASE).param(LJA_TYPE_PARAM, "UNKNOWN"))
            .andExpect(status().isNotAcceptable());
    }

    @Test
    @JiraStory("PO-2757")
    @JiraEpic("PO-2750")
    @JiraTestKey("PO-5961")
    void getLocalJusticeAreaRefData_rejectsKnownAndUnknownTypes() throws Exception {
        mockMvc.perform(get(URL_BASE).param(LJA_TYPE_PARAM, "UNKNOWN", "LJA"))
            .andExpect(status().isNotAcceptable());
    }

    @ParameterizedTest(name = "getLocalJusticeAreaRefData_filtersByMultipleTypes [{index}]")
    @MethodSource("multipleLjaTypeRequests")
    @JiraStory("PO-2757")
    @JiraEpic("PO-2750")
    @JiraTestKey(value = "PO-8407", name = "getLocalJusticeAreaRefData_filtersByMultipleTypes [1]")
    @JiraTestKey(value = "PO-8408", name = "getLocalJusticeAreaRefData_filtersByMultipleTypes [2]")
    @JiraTestKey(value = "PO-8409", name = "getLocalJusticeAreaRefData_filtersByMultipleTypes [3]")
    void getLocalJusticeAreaRefData_filtersByMultipleTypes(MockHttpServletRequestBuilder requestBuilder)
        throws Exception {

        clearRepositoryInvocations();
        ResultActions actions = mockMvc.perform(requestBuilder);
        String body = actions.andReturn().getResponse().getContentAsString();

        actions.andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(greaterThan(2)))
            .andExpect(jsonPath("$.refData[?(@.local_justice_area_id == 1)].lja_type", hasItem("LJA")))
            .andExpect(jsonPath("$.refData[?(@.local_justice_area_id == 10)].lja_type", hasItem("SJCRT")));

        jsonSchemaValidationService.validateOrError(body, GET_LJAS_REF_DATA_RESPONSE);
        assertOnlyOpalRepositoryUsed("findBy");
    }

    @Test
    @JiraStory("PO-2757")
    @JiraEpic("PO-2750")
    @JiraTestKey("PO-5971")
    void getLocalJusticeAreaRefData_returnsStableCachedResults() throws Exception {
        clearRepositoryInvocations();

        String firstBody = performReferenceDataRequest("CRWCRT", "SJCRT");
        Object firstCache = cacheManager.getCache("opalLjaReferenceDataCache");
        String secondBody = performReferenceDataRequest("SJCRT", "CRWCRT");
        Object secondCache = cacheManager.getCache("opalLjaReferenceDataCache");

        assertEquals(firstBody, secondBody);
        assertNotNull(firstCache);
        assertEquals(firstCache, secondCache);
        assertEquals(1, countInvocationsByMethodName(localJusticeAreaRepository, "findBy"));
        assertEquals(0, countInvocationsByMethodName(localJusticeAreaLegacyRepository, "findBy"));
    }

    @Test
    @JiraStory("PO-2757")
    @JiraEpic("PO-2750")
    @JiraTestKey("PO-5959")
    void getLocalJusticeAreaRefData_filtersByQueryAndType() throws Exception {
        clearRepositoryInvocations();

        mockMvc.perform(get(URL_BASE).param("q", "1").param(LJA_TYPE_PARAM, "LJA"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").isNumber())
            .andExpect(jsonPath("$.refData[?(@.local_justice_area_id == 1)].lja_type", hasItem("LJA")));

        assertOnlyOpalRepositoryUsed("findBy");
    }

    @Test
    @JiraStory("PO-7248")
    @JiraEpic("PO-8248")
    @JiraTestKey("PO-9435")
    void getLocalJusticeAreaRefData_usesCache() throws Exception {
        clearRepositoryInvocations();

        String firstBody = performReferenceDataRequest("CRWCRT", "SJCRT");
        String secondBody = performReferenceDataRequest("CRWCRT", "SJCRT");

        assertEquals(firstBody, secondBody);
        assertEquals(1, countInvocationsByMethodName(localJusticeAreaRepository, "findBy"));
        assertEquals(0, countInvocationsByMethodName(localJusticeAreaLegacyRepository, "findBy"));
    }

    private void clearRepositoryInvocations() {
        clearInvocations(localJusticeAreaRepository);
        clearInvocations(localJusticeAreaLegacyRepository);
    }

    private void assertOnlyOpalRepositoryUsed(String methodName) {
        assertEquals(1, countInvocationsByMethodName(localJusticeAreaRepository, methodName));
        assertEquals(0, countInvocationsByMethodName(localJusticeAreaLegacyRepository, methodName));
    }

    private String performReferenceDataRequest(String... ljaTypes) throws Exception {
        return mockMvc.perform(get(URL_BASE).param(LJA_TYPE_PARAM, ljaTypes))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    }
}
