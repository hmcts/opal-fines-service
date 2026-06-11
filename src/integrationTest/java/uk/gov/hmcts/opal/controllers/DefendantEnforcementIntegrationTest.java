package uk.gov.hmcts.opal.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.dto.AddDefendantAccountEnforcementRequest;
import uk.gov.hmcts.opal.dto.PaymentTerms;
import uk.gov.hmcts.opal.dto.PostedDetails;
import uk.gov.hmcts.opal.dto.ResultId;
import uk.gov.hmcts.opal.dto.ResultResponse;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.dto.common.InstalmentPeriod;
import uk.gov.hmcts.opal.dto.common.PaymentTermsType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import uk.gov.hmcts.opal.service.UserStateService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


abstract class DefendantEnforcementIntegrationTest extends AbstractIntegrationTest {

    protected static final String URL_BASE = "/defendant-accounts";
    protected static final Long BUSINESS_UNIT_ID = 77L;
    protected static final Long DEFENDANT_ACCOUNT_ID = 99000000000006L;
    protected static final Long INVALID_DEFENDANT_ACCOUNT_ID = 404L;

    protected static final List<ResultResponse> fullResponses = List.of(
        ResultResponse.builder().parameterName("reason").response("test reason").build(),
        ResultResponse.builder().parameterName("jail_days").response("14").build(),
        ResultResponse.builder().parameterName("enforcer_id").response("50000000001").build(),
        ResultResponse.builder().parameterName("earliest_release_date").response("2026-05-01T00:00:00").build()
    );

    protected static final List<ResultResponse> colloResponses = List.of(
        ResultResponse.builder().parameterName("reason").response("a").build(),
        ResultResponse.builder().parameterName("collectiontype").response("Wages").build(),
        ResultResponse.builder().parameterName("reserveterms").response("aa").build()
    );

    protected static final PaymentTerms paymentTerms = PaymentTerms.builder()
        .daysInDefault(7)
        .dateDaysInDefaultImposed(LocalDate.of(2026, 5, 28))
        .extension(true)
        .reasonForExtension("extension reason")
        .paymentTermsType(PaymentTermsType.fromCode("B"))
        .effectiveDate(LocalDate.of(2026, 10, 30))
        .instalmentPeriod(InstalmentPeriod.fromCode("W"))
        .lumpSumAmount(BigDecimal.valueOf(500000L))
        .instalmentAmount(BigDecimal.valueOf(0.50))
        .postedDetails(PostedDetails.builder()
                           .postedDate(LocalDate.of(2026, 5, 28).atStartOfDay())
                           .postedBy("The Republic")
                           .postedByName("Master Yoda")
                           .build())
        .build();

    void postEnforcementImpl_fullRequest_Success(Logger log) throws Exception {
        AddDefendantAccountEnforcementRequest request = AddDefendantAccountEnforcementRequest.builder()
            .resultId(ResultId.ABDC)
            .enforcementResultResponses(fullResponses)
            .paymentTerms(paymentTerms)
            .build();

        String version = getCurrentDefendantAccountVersion().toString();

        ResultActions resultActions = mockMvc.perform(
            post(URL_BASE + "/" + DEFENDANT_ACCOUNT_ID + "/enforcements")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("Authorization", userStateStub.getBearerToken())
                .header("Business-Unit-ID", BUSINESS_UNIT_ID.toString())
                .header("IF-MATCH", version)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON));

        String body = resultActions.andReturn().getResponse().getContentAsString();

        log.info(":testPostEnforcementImpl_fullRequest_Success: Response body: \n{}", ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("defendant_account_id").value("99000000000006"))
            .andExpect(jsonPath("version").exists())
            .andExpect(jsonPath("enforcement_id").exists());
    }

    void postEnforcementImpl_minimumRequest_Success(Logger log) throws Exception {

        AddDefendantAccountEnforcementRequest request = AddDefendantAccountEnforcementRequest.builder()
            .resultId(ResultId.ABDC)
            .enforcementResultResponses(Collections.emptyList())
            .paymentTerms(paymentTerms)
            .build();

        String version = getCurrentDefendantAccountVersion().toString();

        ResultActions resultActions = mockMvc.perform(
            post(URL_BASE + "/" + DEFENDANT_ACCOUNT_ID + "/enforcements")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("Authorization", userStateStub.getBearerToken())
                .header("Business-Unit-ID", BUSINESS_UNIT_ID.toString())
                .header("IF-MATCH", version)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON));

        String body = resultActions.andReturn().getResponse().getContentAsString();

        log.info(":testPostEnforcementImpl_fullRequest_Success: Response body: \n{}", ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("defendant_account_id").value("99000000000006"))
            .andExpect(jsonPath("version").exists())
            .andExpect(jsonPath("enforcement_id").exists());
    }

    void postEnforcementImpl_invalidDefendant_Failure(Logger log) throws Exception {
        AddDefendantAccountEnforcementRequest request = AddDefendantAccountEnforcementRequest.builder()
            .resultId(ResultId.ABDC)
            .enforcementResultResponses(Collections.emptyList())
            .paymentTerms(paymentTerms)
            .build();

        String version = getCurrentDefendantAccountVersion().toString();

        ResultActions resultActions = mockMvc.perform(
            post(URL_BASE + "/" + INVALID_DEFENDANT_ACCOUNT_ID + "/enforcements")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("Authorization", userStateStub.getBearerToken())
                .header("Business-Unit-ID", BUSINESS_UNIT_ID.toString())
                .header("IF-MATCH", version)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON));

        String body = resultActions.andReturn().getResponse().getContentAsString();

        log.info(":testPostEnforcementImpl_fullRequest_Success: Response body: \n{}", ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().is4xxClientError())
            .andExpect(jsonPath("title").value("Entity Not Found"));
    }

    void postEnforcementImpl_colloResponses_AreReturnedByEnforcementStatus(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(buildEnterEnforcementUserState());

        AddDefendantAccountEnforcementRequest request = AddDefendantAccountEnforcementRequest.builder()
            .resultId(ResultId.COLLO)
            .enforcementResultResponses(colloResponses)
            .build();

        String version = getCurrentDefendantAccountVersion().toString();

        ResultActions postResult = mockMvc.perform(
            post(URL_BASE + "/" + DEFENDANT_ACCOUNT_ID + "/enforcements")
                .header("Authorization", AUTH_HEADER)
                .header("Business-Unit-ID", BUSINESS_UNIT_ID.toString())
                .header("IF-MATCH", version)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        );

        String postBody = postResult.andReturn().getResponse().getContentAsString();
        log.info(":postEnforcementImpl_colloResponses_AreReturnedByEnforcementStatus: POST response body:\n{}",
            ToJsonString.toPrettyJson(postBody));

        postResult.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("defendant_account_id").value(String.valueOf(DEFENDANT_ACCOUNT_ID)))
            .andExpect(jsonPath("enforcement_id").exists());

        ResultActions getResult = mockMvc.perform(
            get(URL_BASE + "/" + DEFENDANT_ACCOUNT_ID + "/enforcement-status")
                .header("Authorization", AUTH_HEADER)
        );

        String getBody = getResult.andReturn().getResponse().getContentAsString();
        log.info(":postEnforcementImpl_colloResponses_AreReturnedByEnforcementStatus: GET response body:\n{}",
            ToJsonString.toPrettyJson(getBody));

        getResult.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.last_enforcement_action.enforcement_action.result_id").value("COLLO"))
            .andExpect(jsonPath("$.last_enforcement_action.reason").value("a"))
            .andExpect(jsonPath("$.last_enforcement_action.result_responses[0].parameter_name").value("reason"))
            .andExpect(jsonPath("$.last_enforcement_action.result_responses[0].response").value("a"))
            .andExpect(jsonPath("$.last_enforcement_action.result_responses[1].parameter_name")
                .value("collectiontype"))
            .andExpect(jsonPath("$.last_enforcement_action.result_responses[1].response").value("Wages"))
            .andExpect(jsonPath("$.last_enforcement_action.result_responses[2].parameter_name")
                .value("reserveterms"))
            .andExpect(jsonPath("$.last_enforcement_action.result_responses[2].response").value("aa"));
    }

    private Integer getCurrentDefendantAccountVersion() {
        return jdbcTemplate.queryForObject(
            "SELECT version_number FROM defendant_accounts WHERE defendant_account_id = ?",
            Integer.class,
            DEFENDANT_ACCOUNT_ID
        );
    }

    private UserState buildEnterEnforcementUserState() {
        return UserState.builder()
            .userId(1L)
            .userName("testUser")
            .businessUnitUser(Set.of(BusinessUnitUser.builder()
                .businessUnitUserId("testUserId")
                .businessUnitId((short) 77)
                .permissions(Set.of(Permission.builder()
                    .permissionId(10L)
                    .permissionName("Enter Enforcement")
                    .build()))
                .build()))
            .build();
    }
}
