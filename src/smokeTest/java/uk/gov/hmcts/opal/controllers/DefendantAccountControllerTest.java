package uk.gov.hmcts.opal.controllers;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.AccountEnquiryDto;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.service.DefendantAccountService;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
class DefendantAccountControllerTest {

    @Mock
    private DefendantAccountService defendantAccountService;

    @InjectMocks
    private DefendantAccountController defendantAccountController;

    @Value("${TEST_URL:http://localhost:4550/api/defendant-account}")
    private String testUrl;

    @BeforeEach
    public void setup() {
        RestAssured.baseURI = testUrl;
        RestAssured.useRelaxedHTTPSValidation();
    }

    @Test
    @Transactional
    void testGetDefendantAccount_WithValidRequest_ReturnsDefendantAccountEntity() {
        // Arrange
        DefendantAccountEntity defendantAccountExpected = new DefendantAccountEntity();

        when(defendantAccountService.getDefendantAccount(any(AccountEnquiryDto.class)))
            .thenReturn(defendantAccountExpected);

        // Act
        AccountEnquiryDto request = AccountEnquiryDto.builder()
            .businessUnitId(Short.parseShort("0")).accountNumber("0").build();

        ResponseEntity<DefendantAccountEntity> re = defendantAccountController.getDefendantAccount(request);
        DefendantAccountEntity defendantAccountActual = re.getBody();

        Assertions.assertEquals(200, re.getStatusCode().value());
        Assertions.assertEquals(defendantAccountExpected, defendantAccountActual);
    }

    @Test
    void testGetDefendantAccount_WithEmptyRequest_ReturnsNoData() {

        String jsonBody = "{\n"
            + " \"businessUnitId\": 0 ,\n"
            + " \"accountNumber\": \"0\"\n"
            + "}\n";
        Response response = given()
            .contentType(ContentType.JSON)
            .body(jsonBody)
            .when()
            .get()
            .then()
            .extract().response();

        Assertions.assertEquals(204, response.statusCode());
    }
}
