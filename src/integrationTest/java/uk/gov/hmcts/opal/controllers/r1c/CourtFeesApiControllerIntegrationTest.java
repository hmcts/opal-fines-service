package uk.gov.hmcts.opal.controllers.r1c;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.common.user.authorisation.model.Domain;
import uk.gov.hmcts.opal.common.user.authorisation.model.DomainBusinessUnitUsers;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserStateV2;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles({"integration"})
@TestPropertySource(properties = "launchdarkly.default-flag-values.release-1c-payment=true")
@Slf4j(topic = "opal.CourtFeesApiControllerIntegrationTest")
@Sql(scripts = "classpath:db/insertData/insert_into_court_fees_entity_graph.sql", executionPhase = BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:db/deleteData/delete_from_court_fees_entity_graph.sql", executionPhase = AFTER_TEST_CLASS)
@DisplayName("CourtFeesApiController Integration Test")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
public class CourtFeesApiControllerIntegrationTest extends AbstractIntegrationTest {
    private static final String URL_BASE = "/court-fees";
    private static final short BUSINESS_UNIT_ID = 953;

    @MockitoBean
    UserStateService userStateService;

    @Test
    @JiraStory("PO-3701")
    @JiraEpic("PO-2660")
    void testGetCourtFees_happyPath() throws Exception {
        mockAuthenticatedUser(BUSINESS_UNIT_ID, true);

        mockMvc.perform(get(URL_BASE).param("business_unit_id", String.valueOf(BUSINESS_UNIT_ID)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.court_fees").isArray())
            .andExpect(jsonPath("$.court_fees", hasSize(2)))
            .andExpect(jsonPath("$.court_fees[0].court_fee_id").value(953001))
            .andExpect(jsonPath("$.court_fees[0].court_fee_code").value("CF001"))
            .andExpect(jsonPath("$.court_fees[0].description").value("Court fee one"))
            .andExpect(jsonPath("$.court_fees[0].amount").value(12.50))
            .andExpect(jsonPath("$.court_fees[1].court_fee_id").value(953002))
            .andExpect(jsonPath("$.court_fees[1].court_fee_code").value("CF002"))
            .andExpect(jsonPath("$.court_fees[1].description").value("Court fee two"))
            .andExpect(jsonPath("$.court_fees[1].amount").value(25.00));
    }

    @Test
    @JiraStory("PO-3701")
    @JiraEpic("PO-2660")
    void testGretCourtFees_businessUnitNotFound() throws Exception {
        mockMvc.perform(get(URL_BASE).param("business_unit_id", "999"))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Entity Not Found"));
    }

    @Test
    @JiraStory("PO-3701")
    @JiraEpic("PO-2660")
    void testGretCourtFees_notAuthorized() throws Exception {
        mockAuthenticatedUser(BUSINESS_UNIT_ID, false);

        mockMvc.perform(get(URL_BASE).param("business_unit_id", String.valueOf(BUSINESS_UNIT_ID)))
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Forbidden"))
            .andExpect(jsonPath("$.detail").value("You do not have permission to access this resource"));
    }

    @Test
    @JiraStory("PO-3701")
    @JiraEpic("PO-2660")
    void testGetCourtFees_happyPathEmptyList() throws Exception {
        short businessUnitId = 954;
        mockAuthenticatedUser(businessUnitId, true);

        mockMvc.perform(get(URL_BASE).param("business_unit_id", String.valueOf(businessUnitId)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.court_fees").isArray())
            .andExpect(jsonPath("$.court_fees", hasSize(0)));
    }

    private void mockAuthenticatedUser(short businessUnitId, boolean authorised) {
        UserStateV2 userState = mock(UserStateV2.class);
        DomainBusinessUnitUsers domainBusinessUnitUsers = mock(DomainBusinessUnitUsers.class);
        BusinessUnitUser businessUnitUser = mock(BusinessUnitUser.class);

        when(userStateService.getUserStateFromSecurityContext()).thenReturn(userState);
        when(userState.getDomainBusinessUnitUsers(Domain.FINES)).thenReturn(domainBusinessUnitUsers);
        when(domainBusinessUnitUsers.getBusinessUnitUserForBusinessUnit(businessUnitId))
            .thenReturn(authorised ? Optional.of(businessUnitUser) : Optional.empty());
    }
}
