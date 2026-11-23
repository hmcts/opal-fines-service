package uk.gov.hmcts.opal.controllers.r1a;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.entity.OpalLocalJusticeAreaEntity;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaLegacyEntity;
import uk.gov.hmcts.opal.entity.OpalLocalJusticeAreaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.SchemaPaths;
import uk.gov.hmcts.opal.repository.LocalJusticeAreaLegacyRepository;
import uk.gov.hmcts.opal.repository.OpalLocalJusticeAreaRepository;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@ActiveProfiles({"integration", "legacy"})
@Sql(scripts = "classpath:db/insertData/insert_into_local_justice_area_legacy.sql",
    executionPhase = BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:db/deleteData/delete_from_local_justice_area_legacy.sql",
    executionPhase = AFTER_TEST_CLASS)
@DisplayName("Legacy Local Justice Area Controller Integration Test")
@Isolated
@Tag("ExtendedTest")
class LegacyLocalJusticeAreaControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String URL_BASE = "/local-justice-areas";
    private static final String LJA_TYPE_PARAM = "lja_type";
    private static final String GET_LJAS_REF_DATA_RESPONSE =
        SchemaPaths.REFERENCE_DATA + "/getLJARefDataResponse.json";

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private OpalLocalJusticeAreaRepository opalLocalJusticeAreaRepository;

    @Autowired
    private LocalJusticeAreaLegacyRepository localJusticeAreaLegacyRepository;

    @Autowired
    private JsonSchemaValidationService jsonSchemaValidationService;

    @Test
    @Transactional
    @Sql(scripts = "classpath:db/insertData/insert_into_local_justice_area_legacy.sql")
    void getLocalJusticeArea_readsLegacyTableWhenIdsOverlap() throws Exception {
        short id = 1;
        LocalJusticeAreaLegacyEntity legacyArea = localJusticeAreaLegacyRepository.findById(id).orElseThrow();
        legacyArea.setName("Legacy Split LJA");
        localJusticeAreaLegacyRepository.saveAndFlush(legacyArea);
        OpalLocalJusticeAreaEntity opalArea = opalLocalJusticeAreaRepository.findById(id).orElseGet(
            () -> OpalLocalJusticeAreaEntity.builder()
                .localJusticeAreaId(id)
                .addressLine1("Split Street")
                .ljaType(OpalLocalJusticeAreaType.LJA)
                .build());
        opalArea.setName("Opal Split LJA");
        opalLocalJusticeAreaRepository.saveAndFlush(opalArea);

        mockMvc.perform(get(URL_BASE + "/" + id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Legacy Split LJA"));
    }

    @BeforeEach
    @AfterEach
    void clearReferenceDataCache() {
        var cache = cacheManager.getCache("legacyLjaReferenceDataCache");
        if (cache != null) {
            cache.clear();
        }
    }

    @Test
    @Transactional
    @Sql(scripts = "classpath:db/insertData/insert_into_local_justice_area_legacy.sql")
    void getLocalJusticeAreaRefData_readsLegacyTableWhenIdsOverlap() throws Exception {
        short id = 1;
        LocalJusticeAreaLegacyEntity legacyArea = localJusticeAreaLegacyRepository.findById(id).orElseThrow();
        legacyArea.setName("Legacy Split LJA");
        localJusticeAreaLegacyRepository.saveAndFlush(legacyArea);
        OpalLocalJusticeAreaEntity opalArea = opalLocalJusticeAreaRepository.findById(id).orElseGet(
            () -> OpalLocalJusticeAreaEntity.builder()
                .localJusticeAreaId(id)
                .addressLine1("Split Street")
                .ljaType(OpalLocalJusticeAreaType.LJA)
                .build());
        opalArea.setName("Opal Split LJA");
        opalLocalJusticeAreaRepository.saveAndFlush(opalArea);

        mockMvc.perform(get(URL_BASE).param("q", "Split LJA").param(LJA_TYPE_PARAM, "LJA"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.refData[?(@.local_justice_area_id == 1)].name", hasItem("Legacy Split LJA")))
            .andExpect(jsonPath("$.refData[*].name", not(hasItem("Opal Split LJA"))));
    }

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
    void getLocalJusticeAreaById_returnsLocalJusticeAreaFromLegacyTable() throws Exception {
        mockMvc.perform(get(URL_BASE + "/1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.localJusticeAreaId").value(1))
            .andExpect(jsonPath("$.name").value("AAAA Trial Court"))
            .andExpect(jsonPath("$.addressLine1").value("Alpha Trial Courts"))
            .andExpect(jsonPath("$.addressLine2").value("Court Quarter"))
            .andExpect(jsonPath("$.addressLine3").value("666 Trial Street"))
            .andExpect(jsonPath("$.postcode").value("TR12 1TR"));

    }

    @Test
    @JiraStory("PO-312")
    @JiraStory("PO-304")
    @JiraEpic("PO-304")
    void getLocalJusticeAreaById_returnsNotFound() throws Exception {
        mockMvc.perform(get(URL_BASE + "/2"))
            .andExpect(status().isNotFound());

    }

    @Test
    @JiraStory("PO-2757")
    @JiraEpic("PO-2750")
    void getLocalJusticeAreaRefData_returnsAllDataFromLegacyTable() throws Exception {
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
    }

    @Test
    @JiraStory("PO-2757")
    @JiraEpic("PO-2750")
    void getLocalJusticeAreaRefData_filtersBySingleType() throws Exception {
        ResultActions actions = mockMvc.perform(get(URL_BASE).param(LJA_TYPE_PARAM, "LJA"));
        String body = actions.andReturn().getResponse().getContentAsString();

        actions.andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(greaterThan(0)))
            .andExpect(jsonPath("$.refData[?(@.local_justice_area_id == 1)].lja_type", hasItem("LJA")));

        jsonSchemaValidationService.validateOrError(body, GET_LJAS_REF_DATA_RESPONSE);
    }

    @Test
    @JiraStory("PO-2757")
    @JiraEpic("PO-2750")
    void getLocalJusticeAreaRefData_rejectsUnknownType() throws Exception {
        mockMvc.perform(get(URL_BASE).param(LJA_TYPE_PARAM, "UNKNOWN"))
            .andExpect(status().isNotAcceptable());
    }

    @Test
    @JiraStory("PO-2757")
    @JiraEpic("PO-2750")
    void getLocalJusticeAreaRefData_rejectsKnownAndUnknownTypes() throws Exception {
        mockMvc.perform(get(URL_BASE).param(LJA_TYPE_PARAM, "UNKNOWN", "LJA"))
            .andExpect(status().isNotAcceptable());
    }

    @ParameterizedTest(name = "getLocalJusticeAreaRefData_filtersByMultipleTypes [{index}]")
    @MethodSource("multipleLjaTypeRequests")
    @JiraStory("PO-2757")
    @JiraEpic("PO-2750")
    void getLocalJusticeAreaRefData_filtersByMultipleTypes(MockHttpServletRequestBuilder requestBuilder)
        throws Exception {

        ResultActions actions = mockMvc.perform(requestBuilder);
        String body = actions.andReturn().getResponse().getContentAsString();

        actions.andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(greaterThan(2)))
            .andExpect(jsonPath("$.refData[?(@.local_justice_area_id == 1)].lja_type", hasItem("LJA")))
            .andExpect(jsonPath("$.refData[?(@.local_justice_area_id == 10)].lja_type", hasItem("SJCRT")));

        jsonSchemaValidationService.validateOrError(body, GET_LJAS_REF_DATA_RESPONSE);
    }

    @Test
    @JiraStory("PO-2757")
    @JiraEpic("PO-2750")
    void getLocalJusticeAreaRefData_returnsStableCachedResults() throws Exception {
        String firstBody = performReferenceDataRequest("CRWCRT", "SJCRT");
        Object firstCache = cacheManager.getCache("legacyLjaReferenceDataCache");
        String secondBody = performReferenceDataRequest("SJCRT", "CRWCRT");
        Object secondCache = cacheManager.getCache("legacyLjaReferenceDataCache");

        assertEquals(firstBody, secondBody);
        assertNotNull(firstCache);
        assertEquals(firstCache, secondCache);
    }

    @Test
    @JiraStory("PO-2757")
    @JiraEpic("PO-2750")
    void getLocalJusticeAreaRefData_filtersByQueryAndType() throws Exception {
        mockMvc.perform(get(URL_BASE).param("q", "1").param(LJA_TYPE_PARAM, "LJA"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").isNumber())
            .andExpect(jsonPath("$.refData[?(@.local_justice_area_id == 1)].lja_type", hasItem("LJA")));

    }

    @Test
    @JiraStory("PO-7248")
    @JiraEpic("PO-8248")
    @Transactional
    void getLocalJusticeAreaRefData_usesCache() throws Exception {
        String firstBody = performReferenceDataRequest("CRWCRT", "SJCRT");
        var area = localJusticeAreaLegacyRepository.findById((short) 10).orElseThrow();
        area.setName("Updated cached LJA");
        localJusticeAreaLegacyRepository.saveAndFlush(area);

        String cachedBody = performReferenceDataRequest("CRWCRT", "SJCRT");
        assertEquals(firstBody, cachedBody);

        clearReferenceDataCache();
        mockMvc.perform(get(URL_BASE).param(LJA_TYPE_PARAM, "CRWCRT", "SJCRT"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Updated cached LJA")));
    }

    @Test
    @JiraStory("PO-312")
    @JiraStory("PO-304")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-5964")
    void postLocalJusticeAreasSearch_returnsMatchingAreas() throws Exception {
        mockMvc.perform(post(URL_BASE + "/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"ljaCode\":\"00\"}"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].localJusticeAreaId").value(1))
            .andExpect(jsonPath("$[0].name").value("AAAA Trial Court"))
            .andExpect(jsonPath("$[0].addressLine1").value("Alpha Trial Courts"))
            .andExpect(jsonPath("$[0].addressLine2").value("Court Quarter"))
            .andExpect(jsonPath("$[0].addressLine3").value("666 Trial Street"))
            .andExpect(jsonPath("$[0].postcode").value("TR12 1TR"));
    }

    @Test
    @JiraStory("PO-312")
    @JiraStory("PO-304")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-5962")
    void postLocalJusticeAreasSearch_returnsNoMatches() throws Exception {
        mockMvc.perform(post(URL_BASE + "/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"criteria\":\"2\"}"))
            .andExpect(status().isOk());
    }

    private String performReferenceDataRequest(String... ljaTypes) throws Exception {
        return mockMvc.perform(get(URL_BASE).param(LJA_TYPE_PARAM, ljaTypes))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    }
}
