package uk.gov.hmcts.opal.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.allPermissionsUser;

import org.junit.jupiter.api.DisplayName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.SchemaPaths;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;

@Component
@DisplayName("Integration tests for /defendant-accounts/{id}/header-summary")
public class DefendantAccountHeaderSummaryIntegrationTest extends BaseDefendantAccountsIntegrationTest {

    protected final String getHeaderSummaryResponseSchemaLocation() {
        return SchemaPaths.DEFENDANT_ACCOUNT + "/getDefendantAccountHeaderSummaryResponse.json";
    }

    private final Logger log = LoggerFactory.getLogger(getClass());

    @DisplayName("Get header summary for individual defendant account [@PO-2287]")
    public void getHeaderSummary_Individual(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions resultActions = mockMvc.perform(get(URL_BASE + "/77/header-summary")
            .header("authorization", "Bearer some_value"));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetHeaderSummary_Individual: Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.defendant_account_id").value("77"))
            .andExpect(jsonPath("$.account_number").value("177A"))
            .andExpect(jsonPath("$.debtor_type").value("Defendant"))
            .andExpect(jsonPath("$.is_youth").value(false))
            .andExpect(jsonPath("$.fixed_penalty_ticket_number").value("888"))
            .andExpect(jsonPath("$.business_unit_summary.business_unit_id").value("78"))
            .andExpect(jsonPath("$.payment_state_summary.imposed_amount").value(700.58))
            .andExpect(jsonPath("$.payment_state_summary.paid_amount").value(200.00))
            .andExpect(jsonPath("$.party_details.organisation_flag").value(false))
            .andExpect(jsonPath("$.party_details.individual_details.forenames").value("Anna"))
            .andExpect(jsonPath("$.party_details.individual_details.surname").value("Graham"))
            .andExpect(jsonPath("$.party_details.organisation_details").doesNotExist());

        jsonSchemaValidationService.validateOrError(body, getHeaderSummaryResponseSchemaLocation());
    }

    @DisplayName("Get header summary for organisation defendant account [@PO-2287]")
    public void getHeaderSummary_Organisation(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions resultActions = mockMvc.perform(get(URL_BASE + "/10001/header-summary")
            .header("authorization", "Bearer some_value"));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetHeaderSummary_Organisation: Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.defendant_account_id").value("10001"))
            .andExpect(jsonPath("$.account_number").value("10001A"))
            .andExpect(jsonPath("$.debtor_type").value("Defendant"))
            .andExpect(jsonPath("$.is_youth").value(false))
            .andExpect(jsonPath("$.party_details.organisation_flag").value(true))
            .andExpect(jsonPath("$.party_details.organisation_details.organisation_name").value("Kings Arms"))
            .andExpect(jsonPath("$.party_details.individual_details").doesNotExist());

        jsonSchemaValidationService.validateOrError(body, getHeaderSummaryResponseSchemaLocation());
    }

    @DisplayName("OPAL: Get header summary for non-existent ID returns 404")
    public void getHeaderSummary_Opal_NotFound(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions ra = mockMvc.perform(
            get(URL_BASE + "/500/header-summary")
                .header("authorization", "Bearer some_value")
        );

        String body = ra.andReturn().getResponse().getContentAsString();
        log.info(":getHeaderSummary_Opal_NotFound: Response body:\n{}", ToJsonString.toPrettyJson(body));

        ra.andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/entity-not-found"))
            .andExpect(jsonPath("$.status").value(404));
    }

    @DisplayName("LEGACY: Get header summary for non-existent ID returns 500")
    public void getHeaderSummary_Legacy_500(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions ra = mockMvc.perform(
            get(URL_BASE + "/500/header-summary")
                .header("authorization", "Bearer some_value")
        );

        String body = ra.andReturn().getResponse().getContentAsString();
        log.info(":getHeaderSummary_Legacy_500: Response body:\n{}", ToJsonString.toPrettyJson(body));

        ra.andExpect(status().is5xxServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    @DisplayName("PO-2297: header-summary (individual) returns correct defendant_party_id from "
        + "defendantAccountPartyId bug fix validation")
    public void testGetHeaderSummary_Individual_UsesDefendantAccountPartyId(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions resultActions = mockMvc.perform(
            get("/defendant-accounts/77/header-summary")
                .header("authorization", "Bearer some_value")
        );

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info("PO-2297 Individual header summary response:\n{}", ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.defendant_account_party_id").value("77"))
            .andExpect(jsonPath("$.party_details.organisation_flag").value(false))
            .andExpect(jsonPath("$.party_details.individual_details.forenames").value("Anna"))
            .andExpect(jsonPath("$.party_details.individual_details.surname").value("Graham"));

        jsonSchemaValidationService.validateOrError(body, getHeaderSummaryResponseSchemaLocation());
    }

    @DisplayName("PO-2297: header-summary (organisation) returns correct defendant_party_id from"
        + " defendantAccountPartyId — bug fix validation")
    public void testGetHeaderSummary_Organisation_UsesDefendantAccountPartyId(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions resultActions = mockMvc.perform(
            get("/defendant-accounts/10001/header-summary")
                .header("authorization", "Bearer some_value")
        );

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info("PO-2297 Organisation header summary response:\n{}", ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.defendant_account_party_id").value("10001"))
            .andExpect(jsonPath("$.party_details.party_id").value("10001"))
            .andExpect(jsonPath("$.party_details.organisation_flag").value(true))
            .andExpect(jsonPath("$.party_details.organisation_details.organisation_name").value("Kings Arms"));

        jsonSchemaValidationService.validateOrError(body, getHeaderSummaryResponseSchemaLocation());
    }

    @DisplayName("PO-2119 / Problem JSON contains retriable field")
    public void testEntityNotFoundExceptionContainsRetriable(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions resultActions = mockMvc.perform(get(URL_BASE + "/12345/header-summary")
            .header("authorization", "Bearer some_value"));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testRetriableIncludedInProblemDetail: Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/entity-not-found"))
            .andExpect(jsonPath("$.title").value("Entity Not Found"))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.detail").value("The requested entity could not be found"))
            .andExpect(jsonPath("$.retriable").value(false));
    }


    @DisplayName("PO-2119 / Problem JSON contains retriable field")
    public void testWrongMediaTypeContainsRetriableField(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(anyString()))
            .thenReturn(new UserState.DeveloperUserState());

        ResultActions resultActions = mockMvc.perform(post("/defendant-accounts/search")
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_ATOM_XML)
            .content("""
                {
                  "active_accounts_only": true,
                  "business_unit_ids": [],
                  "reference_number": {
                    "account_number": "177A",
                    "prosecutor_case_reference": null,
                    "organisation": false
                  },
                  "defendant": null
                }
                """));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testRetriableIncludedInProblemDetail: Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isUnsupportedMediaType())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type")
                .value("https://hmcts.gov.uk/problems/unsupported-media-type"))
            .andExpect(jsonPath("$.title").value("Unsupported Media Type"))
            .andExpect(jsonPath("$.status").value(415))
            .andExpect(jsonPath("$.detail")
                .value("The Content-Type is not supported. Please use application/json"))
            .andExpect(jsonPath("$.retriable").value(false));
    }

    void testGetHeaderSummary_ThrowsNotFound(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(any()))
            .thenReturn(allPermissionsUser());

        ResultActions resultActions = mockMvc.perform(get("/defendant-accounts/999777/header-summary")
            .header("authorization", "Bearer some_value"));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetHeaderSummary_ThrowsNotFound: Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isNotFound());
    }
}
