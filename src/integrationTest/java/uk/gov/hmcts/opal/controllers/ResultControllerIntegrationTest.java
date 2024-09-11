package uk.gov.hmcts.opal.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.opal.entity.projection.ResultReferenceData;
import uk.gov.hmcts.opal.service.opal.ResultService;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = ResultController.class)
@ActiveProfiles({"integration"})
class ResultControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    @Qualifier("resultServiceProxy")
    ResultService resultService;

    @Test
    void testGetResultById() throws Exception {
        ResultReferenceData resultRefData = createResultReferenceData();

        when(resultService.getResultReferenceData("ABC")).thenReturn(resultRefData);

        mockMvc.perform(get("/results/ABC"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.result_id").value("ABC"))
            .andExpect(jsonPath("$.result_title").value("Result AAA-BBB"));

    }


    @Test
    void testGetResultById_WhenResultDoesNotExist() throws Exception {
        when(resultService.getResult("xyz")).thenReturn(null);

        mockMvc.perform(get("/api/result/xyz"))
            .andExpect(status().isNotFound());
    }


    @Test
    void testGetAllResults() throws Exception {
        List<ResultReferenceData> resultList = List.of(
            new ResultReferenceData("ABC",
                                    "Result AAA-BBB",
                                    "Result AAA-BBB Cy",
                                    false,
                                    "ResType-XX",
                                    "AAA-01234",
                                    (short)9),
            new ResultReferenceData("DEF",
                                    "Result CCC-DDD",
                                    "Result CCC-DDD Cy",
                                    true, "ResType-YY",
                                    "BBB-56789",
                                    (short)5)
        );

        when(resultService.getAllResults()).thenReturn(resultList);

        mockMvc.perform(get("/results"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.refData[0].result_id").value("ABC"))
            .andExpect(jsonPath("$.refData[0].result_title").value("Result AAA-BBB"))
            .andExpect(jsonPath("$.refData[1].result_id").value("DEF"))
            .andExpect(jsonPath("$.refData[1].result_title")
                           .value("Result CCC-DDD"));
    }

    @Test
    void getResultsByIds() throws Exception {
        List<ResultReferenceData> resultList = List.of(
            new ResultReferenceData("ABC",
                                    "Result AAA-BBB",
                                    "Result AAA-BBB Cy",
                                    false,
                                    "ResType-XX",
                                    "AAA-01234",
                                    (short)9),
            new ResultReferenceData("DEF",
                                    "Result CCC-DDD",
                                    "Result CCC-DDD Cy",
                                    true, "ResType-YY",
                                    "BBB-56789",
                                    (short)5)
        );

        when(resultService.getResultsByIds(List.of("ABC", "DEF"))).thenReturn(resultList);

        mockMvc.perform(get("/results?result_ids=ABC,DEF"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.refData[0].result_id").value("ABC"))
            .andExpect(jsonPath("$.refData[0].result_title").value("Result AAA-BBB"))
            .andExpect(jsonPath("$.refData[1].result_id").value("DEF"))
            .andExpect(jsonPath("$.refData[1].result_title")
                           .value("Result CCC-DDD"));
    }

    private ResultReferenceData createResultReferenceData() {
        return new ResultReferenceData("ABC",
                                    "Result AAA-BBB",
                                    "Result AAA-BBB Cy",
                                    false,
                                    "ResType-XX",
                                    "AAA-01234",
                                    (short)9);
    }



}
