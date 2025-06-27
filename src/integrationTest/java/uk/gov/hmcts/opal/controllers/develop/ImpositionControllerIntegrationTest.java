package uk.gov.hmcts.opal.controllers.develop;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.opal.dto.search.ImpositionSearchDto;
import uk.gov.hmcts.opal.entity.court.CourtEntity;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.ImpositionEntity;
import uk.gov.hmcts.opal.entity.UserEntity;
import uk.gov.hmcts.opal.service.opal.ImpositionService;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = ImpositionController.class)
@ActiveProfiles({"integration"})
class ImpositionControllerIntegrationTest {

    private static final String URL_BASE = "/dev/impositions/";

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    HttpSecurity httpSecurity;

    @MockitoBean
    @Qualifier("impositionServiceProxy")
    ImpositionService impositionService;

    @Test
    void testGetImpositionById() throws Exception {
        ImpositionEntity impositionEntity = createImpositionEntity();

        when(impositionService.getImposition(1L)).thenReturn(impositionEntity);

        mockMvc.perform(get(URL_BASE + "1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.impositionId").value(1))
            .andExpect(jsonPath("$.postedBy").value("ADMIN"))
            .andExpect(jsonPath("$.resultId").value("AAABBB"))
            .andExpect(jsonPath("$.offenceId").value(8))
            .andExpect(jsonPath("$.unitFineAdjusted").value(false))
            .andExpect(jsonPath("$.unitFineUnits").value(0))
            .andExpect(jsonPath("$.completed").value(false));
    }


    @Test
    void testGetImpositionById_WhenImpositionDoesNotExist() throws Exception {
        when(impositionService.getImposition(2L)).thenReturn(null);

        mockMvc.perform(get(URL_BASE + "2"))
            .andExpect(status().isNotFound());
    }

    @Test
    void testPostImpositionsSearch() throws Exception {
        ImpositionEntity impositionEntity = createImpositionEntity();

        when(impositionService.searchImpositions(any(ImpositionSearchDto.class)))
            .thenReturn(singletonList(impositionEntity));

        mockMvc.perform(post(URL_BASE + "search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"criteria\":\"value\"}"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].impositionId").value(1))
            .andExpect(jsonPath("$[0].postedBy").value("ADMIN"))
            .andExpect(jsonPath("$[0].resultId").value("AAABBB"))
            .andExpect(jsonPath("$[0].offenceId").value(8))
            .andExpect(jsonPath("$[0].unitFineAdjusted").value(false))
            .andExpect(jsonPath("$[0].unitFineUnits").value(0))
            .andExpect(jsonPath("$[0].completed").value(false));
    }

    @Test
    void testPostImpositionsSearch_WhenImpositionDoesNotExist() throws Exception {
        mockMvc.perform(post(URL_BASE + "search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"criteria\":\"2\"}"))
            .andExpect(status().isOk());
    }

    private ImpositionEntity createImpositionEntity() {
        return ImpositionEntity.builder()
            .impositionId(1L)
            .defendantAccount(DefendantAccountEntity.builder().build())
            .postedDate(LocalDateTime.now())
            .postedBy("ADMIN")
            .postedByUser(UserEntity.builder().build())
            .originalPostedDate(LocalDateTime.now())
            .resultId("AAABBB")
            .imposingCourt(CourtEntity.builder().build())
            .imposedDate(LocalDateTime.now())
            .imposedAmount(BigDecimal.TEN)
            .paidAmount(BigDecimal.ONE)
            .offenceId((short)8)
            .creditorAccount(CreditorAccountEntity.builder().build())
            .unitFineAdjusted(false)
            .unitFineUnits((short)0)
            .completed(false)
            .build();
    }
}
