package uk.gov.hmcts.opal.hmrc;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import feign.FeignException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.opal.hmrc.generated.IndividualsDetails.client.IndividualsApiClient;
import uk.gov.hmcts.opal.service.hmrc.HmrcAuthentication;
import uk.gov.hmcts.opal.testdata.IndividualsDetailsApiIntegrationTestData;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@TestPropertySource(properties = {
    "spring.cloud.openfeign.client.config.individuals-api.connectTimeout=250",
    "spring.cloud.openfeign.client.config.individuals-api.readTimeout=250"
})
public class IndividualsDetailsApiIntegrationTest extends AbstractHmrcIntegrationTest {

    @Service
    public static class TestAuthKey implements HmrcAuthentication {
        public String getToken() {
            return "test-auth-token";
        }
    }

    @Autowired
    private IndividualsApiClient api;

    private final String individualsAddressesApiUrl = "/individuals/details/addresses";

    @Test
    @DisplayName("PO-2372 - Minimum data test")
    @JiraStory("PO-2372")
    @JiraEpic("PO-1421")
    void returnsMinData() {
        withSuccessfulMock(individualsAddressesApiUrl, IndividualsDetailsApiIntegrationTestData.minResponse);

        var response =  api.getIndividualsDetailsAddresses(exampleMatchId, exampleCorrelationId, exampleAcceptHeader);

        assertAll(
            () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
            () -> assertEquals("", response.getBody().getLinks().getSelf().getHref()),
            () -> assertEquals("", response.getBody().getResidences().getFirst().getResidenceType()),
            () -> assertEquals(Boolean.TRUE, response.getBody().getResidences().getFirst().getInUse()),
            () -> assertEquals("", response.getBody().getResidences().getFirst().getAddress().getLine1()),
            () -> assertEquals("", response.getBody().getResidences().getFirst().getAddress().getLine2()),
            () -> assertEquals("", response.getBody().getResidences().getFirst().getAddress().getLine3()),
            () -> assertEquals("", response.getBody().getResidences().getFirst().getAddress().getLine4()),
            () -> assertEquals("", response.getBody().getResidences().getFirst().getAddress().getLine5()),
            () -> assertEquals("", response.getBody().getResidences().getFirst().getAddress().getPostcode())
        );

        verifyRequestHeader("Bearer test-auth-token");
    }

    @Test
    @DisplayName("PO-2372 - Maximum data test")
    @JiraStory("PO-2372")
    @JiraEpic("PO-1421")
    void returnsMaxData() {
        withSuccessfulMock(individualsAddressesApiUrl, IndividualsDetailsApiIntegrationTestData.maxResponse);

        var response =  api.getIndividualsDetailsAddresses(exampleMatchId, exampleCorrelationId, exampleAcceptHeader);

        assertAll(
            () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
            () -> assertEquals(
                "/individuals/details?matchId=57072660-1df9-4aeb-b4ea-cd2d7f96e430",
                response.getBody().getLinks().getSelf().getHref()
            ),
            () -> assertEquals("NOMINATED", response.getBody().getResidences().getFirst().getResidenceType()),
            () -> assertEquals(Boolean.TRUE, response.getBody().getResidences().getFirst().getInUse()),
            () -> assertEquals(
                "24 Trinity Street",  response.getBody().getResidences().getFirst().getAddress().getLine1()
            ),
            () -> assertEquals(
                "Dawley Bank", response.getBody().getResidences().getFirst().getAddress().getLine2()
            ),
            () -> assertEquals(
                "Telford",
                response.getBody().getResidences().getFirst().getAddress().getLine3()
            ),
            () -> assertEquals(
                "Shropshire", response.getBody().getResidences().getFirst().getAddress().getLine4()
            ),
            () -> assertEquals(
                "UK", response.getBody().getResidences().getFirst().getAddress().getLine5()
            ),
            () -> assertEquals(
                "TF3 4ER", response.getBody().getResidences().getFirst().getAddress().getPostcode()
            )
        );

        verifyRequestHeader("Bearer test-auth-token");
    }

    @Test
    @DisplayName("PO-2372 - Required data only test")
    @JiraStory("PO-2372")
    @JiraEpic("PO-1421")
    void returnsRequiredOnly() {
        withSuccessfulMock(individualsAddressesApiUrl, IndividualsDetailsApiIntegrationTestData.requiredOnlyResponse);

        var response =  api.getIndividualsDetailsAddresses(exampleMatchId, exampleCorrelationId, exampleAcceptHeader);

        assertAll(
            () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
            () -> assertEquals(
                "/individuals/details?matchId=57072660-1df9-4aeb-b4ea-cd2d7f96e430",
                response.getBody().getLinks().getSelf().getHref()
            ),
            () -> assertNull(response.getBody().getResidences())
        );

        verifyRequestHeader("Bearer test-auth-token");
    }

    @Test
    @DisplayName("PO-2372 - Multiple items test")
    @JiraStory("PO-2372")
    @JiraEpic("PO-1421")
    void multipleItems() {
        withSuccessfulMock(individualsAddressesApiUrl, IndividualsDetailsApiIntegrationTestData.multipleItemsResponse);

        var response =  api.getIndividualsDetailsAddresses(exampleMatchId, exampleCorrelationId, exampleAcceptHeader);

        assertAll(
            () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
            () -> assertEquals(
                "/individuals/details?matchId=57072660-1df9-4aeb-b4ea-cd2d7f96e430",
                response.getBody().getLinks().getSelf().getHref()
            ),
            () -> assertEquals("NOMINATED", response.getBody().getResidences().getFirst().getResidenceType()),
            () -> assertEquals(Boolean.TRUE, response.getBody().getResidences().getFirst().getInUse()),
            () -> assertEquals(
                "24 Trinity Street",  response.getBody().getResidences().getFirst().getAddress().getLine1()
            ),
            () -> assertEquals(
                "Dawley Bank", response.getBody().getResidences().getFirst().getAddress().getLine2()
            ),
            () -> assertEquals(
                "Telford",
                response.getBody().getResidences().getFirst().getAddress().getLine3()
            ),
            () -> assertEquals(
                "Shropshire", response.getBody().getResidences().getFirst().getAddress().getLine4()
            ),
            () -> assertEquals(
                "UK", response.getBody().getResidences().getFirst().getAddress().getLine5()
            ),
            () -> assertEquals(
                "TF3 4ER", response.getBody().getResidences().getFirst().getAddress().getPostcode()
            )
        );

        verifyRequestHeader("Bearer test-auth-token");
    }

    @Test
    @DisplayName("PO-2372 - Bad request")
    @JiraStory("PO-2372")
    @JiraEpic("PO-1421")
    void badRequest() {
        withUnsuccessfulMock(
            individualsAddressesApiUrl,
            HttpStatus.BAD_REQUEST,
            IndividualsDetailsApiIntegrationTestData.badRequestResponse
        );

        FeignException e = assertThrows(
            FeignException.class,
            () -> api.getIndividualsDetailsAddresses(exampleMatchId, exampleCorrelationId, exampleAcceptHeader)
        );

        String resBody = new String(e.responseBody().get().array(), StandardCharsets.UTF_8);

        assertAll(
            () -> assertEquals(HttpStatus.BAD_REQUEST.value(), e.status()),
            () -> assertEquals("{\"code\":\"INVALID_REQUEST\"}", resBody),
            () -> assertEquals(
                "[400 Bad Request] during [GET] to [http://localhost:3000/individuals/details/addresses?"
                    + "matchId=57072660-1df9-4aeb-b4ea-cd2d7f96e430] [IndividualsApiClient#"
                    + "getIndividualsDetailsAddresses(String,String,String)]: [{\"code\":\"INVALID_REQUEST\"}]",
                e.getMessage()
            )
        );

        verifyRequestHeader("Bearer test-auth-token");
    }

    @Test
    @DisplayName("PO-2372 - Not found request")
    @JiraStory("PO-2372")
    @JiraEpic("PO-1421")
    void notFoundRequest() {
        withUnsuccessfulMock(
            individualsAddressesApiUrl,
            HttpStatus.NOT_FOUND,
            IndividualsDetailsApiIntegrationTestData.notFoundResponse
        );

        FeignException e = assertThrows(
            FeignException.class,
            () -> api.getIndividualsDetailsAddresses(exampleMatchId, exampleCorrelationId, exampleAcceptHeader)
        );

        String resBody = new String(e.responseBody().get().array(), StandardCharsets.UTF_8);

        assertAll(
            () -> assertEquals(HttpStatus.NOT_FOUND.value(), e.status()),
            () -> assertEquals("{\"code\":\"NOT_FOUND\"}", resBody),
            () -> assertEquals(
                "[404 Not Found] during [GET] to [http://localhost:3000/individuals/details/addresses?"
                    + "matchId=57072660-1df9-4aeb-b4ea-cd2d7f96e430] [IndividualsApiClient#"
                    + "getIndividualsDetailsAddresses(String,String,String)]: [{\"code\":\"NOT_FOUND\"}]",
                e.getMessage()
            )
        );

        verifyRequestHeader("Bearer test-auth-token");
    }

    @Test
    @DisplayName("PO-2372 - Request times out")
    @JiraStory("PO-2372")
    @JiraEpic("PO-1421")
    void timesOutRequest() {
        withTimeoutMock(individualsAddressesApiUrl, 500);

        FeignException e = assertThrows(
            FeignException.class,
            () -> api.getIndividualsDetailsAddresses(exampleMatchId, exampleCorrelationId, exampleAcceptHeader)
        );

        assertAll(
            () -> assertEquals(
                "Read timed out executing GET http://localhost:3000/individuals/details/addresses?"
                    + "matchId=57072660-1df9-4aeb-b4ea-cd2d7f96e430",
                e.getMessage()
            )
        );

        verifyRequestHeader("Bearer test-auth-token");
    }

    @Test
    @DisplayName("PO-2372 - Request returns internal server error")
    @JiraStory("PO-2372")
    @JiraEpic("PO-1421")
    void handleServerErrors() {
        withUnsuccessfulMock(individualsAddressesApiUrl, HttpStatus.INTERNAL_SERVER_ERROR, "");

        FeignException e = assertThrows(
            FeignException.class,
            () -> api.getIndividualsDetailsAddresses(exampleMatchId, exampleCorrelationId, exampleAcceptHeader)
        );

        assertAll(
            () -> assertEquals("Retryable PDPO response status=500", e.getMessage()),
            () -> assertEquals(500, e.status())
        );

        verifyRequestHeader("Bearer test-auth-token");
    }
}
