package uk.gov.hmcts.opal.hmrc;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.opal.common.dto.ToJsonString.toJsonString;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import feign.FeignException;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.opal.config.FeignConfiguration;
import uk.gov.hmcts.opal.hmrc.generated.IndividualsDetails.client.IndividualsApiClient;
import uk.gov.hmcts.opal.hmrc.generated.IndividualsDetails.model.Address;
import uk.gov.hmcts.opal.hmrc.generated.IndividualsDetails.model.IndividualsDetailsAddressesResponse;
import uk.gov.hmcts.opal.hmrc.generated.IndividualsDetails.model.Links1;
import uk.gov.hmcts.opal.hmrc.generated.IndividualsDetails.model.Residence;
import uk.gov.hmcts.opal.hmrc.generated.IndividualsDetails.model.Self;

@EnableFeignClients(basePackages = "uk.gov.hmcts.opal.*", defaultConfiguration = FeignConfiguration.class)
@ExtendWith(SpringExtension.class)
@TestPropertySource(properties = {
    "spring.cloud.openfeign.client.config.individuals-api.connectTimeout=250",
    "spring.cloud.openfeign.client.config.individuals-api.readTimeout=250",
    "individuals.name=individuals-api",
    "user.service.url=localhost:3000",
    "individuals.url=localhost:3000"
})
@ImportAutoConfiguration(FeignAutoConfiguration.class)
public class IndividualsApiClientTest {

    private static final String wiremockHost = "localhost";
    private static final int wiremockPort = 3000;
    private final String individualsAddressesApiUrl = "/individuals/details/addresses";
    protected static final String exampleMatchId = "57072660-1df9-4aeb-b4ea-cd2d7f96e430";
    protected static final String exampleCorrelationId = "57072660-1df9-4aeb-b4ea-cd2d7f96e430";
    protected static final String exampleAcceptHeader = "application/vnd.hmrc.2.0+json";

    private static WireMockServer mockServer;

    @Autowired
    private IndividualsApiClient api;

    @BeforeAll
    public static void beforeAll() {
        mockServer = new WireMockServer(wiremockPort);
        mockServer.start();
    }

    @AfterAll
    public static void teardown() {
        mockServer.stop();
    }

    @BeforeEach
    public void beforeEach() {
        resetWireMock();
    }

    private void resetWireMock() {
        WireMock.configureFor(wiremockHost, wiremockPort);
        WireMock.reset();
    }

    protected void withStub(String url, HttpStatus status, String body) {
        stubFor(get(urlPathEqualTo(url))
            .willReturn(aResponse()
                .withStatus(status.value())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(body)));
    }

    protected void withTimeout(String url, int delay) {
        stubFor(get(urlPathEqualTo(url))
            .willReturn(aResponse()
                .withFixedDelay(delay)));
    }

    @Test
    public void testSuccess() {
        withStub(individualsAddressesApiUrl, HttpStatus.OK, toJsonString(
            new IndividualsDetailsAddressesResponse.Builder()
                .links(new Links1.Builder()
                    .self(new Self.Builder()
                        .href("/individuals/details/addresses")
                        .build()
                    ).build()
                )
                .residences(List.of(
                    new Residence.Builder()
                        .residenceType("NOMINATED")
                        .inUse(Boolean.TRUE)
                        .address(new Address.Builder()
                            .line1("123 fake st")
                            .build()
                        ).build()
                ))
                .build()
        ));

        ResponseEntity<IndividualsDetailsAddressesResponse> response = api.getIndividualsDetailsAddresses(
            exampleMatchId, exampleCorrelationId, exampleAcceptHeader);


        assertAll(
            () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
            () -> assertEquals("/individuals/details/addresses", response.getBody().getLinks().getSelf().getHref()),
            () -> assertEquals("NOMINATED", response.getBody().getResidences().getFirst().getResidenceType()),
            () -> assertEquals(Boolean.TRUE, response.getBody().getResidences().getFirst().getInUse()),
            () -> assertEquals("123 fake st", response.getBody().getResidences().getFirst().getAddress().getLine1()),
            () -> assertNull(response.getBody().getResidences().getFirst().getAddress().getLine2()),
            () -> assertNull(response.getBody().getResidences().getFirst().getAddress().getLine3()),
            () -> assertNull(response.getBody().getResidences().getFirst().getAddress().getLine4()),
            () -> assertNull(response.getBody().getResidences().getFirst().getAddress().getLine5()),
            () -> assertNull(response.getBody().getResidences().getFirst().getAddress().getPostcode())
        );
    }

    @Test
    public void testUnauthorized() {
        withStub(individualsAddressesApiUrl, HttpStatus.UNAUTHORIZED, "");

        FeignException e = assertThrows(
            FeignException.class,
            () -> api.getIndividualsDetailsAddresses(exampleMatchId, exampleCorrelationId, exampleAcceptHeader)
        );

        assertAll(
            () -> assertEquals(HttpStatus.UNAUTHORIZED.value(), e.status()),
            () -> assertEquals(
                "[401 Unauthorized] during [GET] to "
                    + "[http://localhost:3000/individuals/details/addresses?matchId=57072660-1df9-4aeb-b4ea-cd2d7f96e430] "
                    + "[IndividualsApiClient#getIndividualsDetailsAddresses(String,String,String)]: []",
                e.getMessage()
            )
        );
    }

    @Test
    public void testNotFound() {
        withStub(individualsAddressesApiUrl, HttpStatus.NOT_FOUND, "");

        FeignException e = assertThrows(
            FeignException.class,
            () -> api.getIndividualsDetailsAddresses(exampleMatchId, exampleCorrelationId, exampleAcceptHeader)
        );

        assertAll(
            () -> assertEquals(HttpStatus.NOT_FOUND.value(), e.status()),
            () -> assertEquals(
                "[404 Not Found] during [GET] to "
                    + "[http://localhost:3000/individuals/details/addresses?matchId=57072660-1df9-4aeb-b4ea-cd2d7f96e430] "
                    + "[IndividualsApiClient#getIndividualsDetailsAddresses(String,String,String)]: []",
                e.getMessage()
            )
        );
    }

    @Test
    public void testInternalServerError() {
        withStub(individualsAddressesApiUrl, HttpStatus.INTERNAL_SERVER_ERROR, "");

        FeignException e = assertThrows(
            FeignException.class,
            () -> api.getIndividualsDetailsAddresses(exampleMatchId, exampleCorrelationId, exampleAcceptHeader)
        );

        assertAll(
            () -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.status()),
            () -> assertEquals(
                "[500 Server Error] during [GET] to "
                    + "[http://localhost:3000/individuals/details/addresses?matchId=57072660-1df9-4aeb-b4ea-cd2d7f96e430] "
                    + "[IndividualsApiClient#getIndividualsDetailsAddresses(String,String,String)]: []",
                e.getMessage()
            )
        );
    }

    @Test
    public void testTimeouts() {
        withTimeout(individualsAddressesApiUrl, 5000);

        FeignException e = assertThrows(
            FeignException.class,
            () -> api.getIndividualsDetailsAddresses(exampleMatchId, exampleCorrelationId, exampleAcceptHeader)
        );

        assertAll(
            () -> assertEquals(
                "Read timed out executing GET http://localhost:3000"
                    + "/individuals/details/addresses?matchId=57072660-1df9-4aeb-b4ea-cd2d7f96e430",
                e.getMessage()
            )
        );
    }
}
