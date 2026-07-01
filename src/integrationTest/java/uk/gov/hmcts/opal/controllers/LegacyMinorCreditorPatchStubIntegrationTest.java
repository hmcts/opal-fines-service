package uk.gov.hmcts.opal.controllers;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.generated.model.AddressDetailsCommon;
import uk.gov.hmcts.opal.generated.model.CreditorAccountPaymentDetailsCommon;
import uk.gov.hmcts.opal.generated.model.OrganisationDetailsCommon;
import uk.gov.hmcts.opal.generated.model.PartyDetailsCommon;
import uk.gov.hmcts.opal.generated.model.PatchMinorCreditorAccountRequest;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@ActiveProfiles({"integration", "legacy"})
@TestPropertySource(properties = {
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1b=true"
})
@Sql(scripts = "classpath:db/insertData/insert_into_minor_creditors.sql", executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:db/deleteData/delete_from_minor_creditors.sql", executionPhase = AFTER_TEST_METHOD)
@Slf4j(topic = "opal.LegacyMinorCreditorPatchStubIntegrationTest")
class LegacyMinorCreditorPatchStubIntegrationTest extends AbstractIntegrationTest {

    private static final String URL_BASE = "/minor-creditor-accounts";
    private static final short BUSINESS_UNIT_ID = 10;
    private static final String UPDATE_MINOR_CREDITOR_ACCOUNT = "LIBRA.of_update_minor_creditor_account";

    @Test
    @JiraStory("PO-1915")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-7610")
    void patchMinorCreditor_notFound_hitsLegacyStub() throws Exception {
        stubLegacyPatchResponse(404, "<error><message>Minor creditor account not found</message></error>");

        performLegacyPatch(404L, "\"1\"")
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    @Test
    @JiraStory("PO-1915")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-7613")
    void patchMinorCreditor_timeout_hitsLegacyStub() throws Exception {
        stubLegacyPatchResponse(408, "<error><message>Request Timeout</message></error>");

        performLegacyPatch(408L, "\"1\"")
            .andExpect(status().isRequestTimeout())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    @Test
    @JiraStory("PO-1915")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-7611")
    void patchMinorCreditor_conflict_hitsLegacyStub() throws Exception {
        stubLegacyPatchResponse(409, "<error><message>Conflict</message></error>");

        performLegacyPatch(409L, "\"2\"")
            .andExpect(status().isConflict())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    @Test
    @JiraStory("PO-1915")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-7609")
    void patchMinorCreditor_serviceUnavailable_hitsLegacyStub() throws Exception {
        stubLegacyPatchResponse(503, "<error><message>Service Unavailable</message></error>");

        performLegacyPatch(503L, "\"1\"")
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    @Test
    @JiraStory("PO-1915")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-7612")
    void patchMinorCreditor_serverError_hitsLegacyStub() throws Exception {
        stubLegacyPatchResponse(500, "<error><message>Internal Server Error</message></error>");

        performLegacyPatch(500L, "\"1\"")
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    private ResultActions performLegacyPatch(long creditorAccountId, String ifMatch) throws Exception {
        userStateStub.addPermissions(BUSINESS_UNIT_ID, FinesPermission.values());
        return mockMvc.perform(
            patch(URL_BASE + "/" + creditorAccountId)
                .contentType(MediaType.APPLICATION_JSON)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("Authorization", userStateStub.getBearerToken())
                .header("If-Match", ifMatch)
                .header("Business-Unit-Id", String.valueOf(BUSINESS_UNIT_ID))
                .content(objectMapper.writeValueAsString(patchMinorCreditorLegacyRequest()))
        );
    }

    private PatchMinorCreditorAccountRequest patchMinorCreditorLegacyRequest() {
        return new PatchMinorCreditorAccountRequest()
            .partyDetails(new PartyDetailsCommon()
                .partyId("99008")
                .organisationFlag(true)
                .organisationDetails(new OrganisationDetailsCommon().organisationName("Updated Ltd")))
            .address(new AddressDetailsCommon()
                .addressLine1("99 Updated Road")
                .addressLine2("Updated Area")
                .addressLine3("Updated Town")
                .postcode("NW1 1AA"))
            .payment(new CreditorAccountPaymentDetailsCommon()
                .accountName("Updated Account")
                .sortCode("112233")
                .accountNumber("12345678")
                .accountReference("Ref-01")
                .payByBacs(true)
                .holdPayment(true));
    }

    private void stubLegacyPatchResponse(int status, String body) {
        stubFor(post(urlPathEqualTo("/opal"))
            .withQueryParam("actionType", equalTo(UPDATE_MINOR_CREDITOR_ACCOUNT))
            .willReturn(aResponse()
                .withStatus(status)
                .withHeader("Content-Type", MediaType.APPLICATION_XML_VALUE)
                .withBody(body)));
    }
}
