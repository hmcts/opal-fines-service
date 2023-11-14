package uk.gov.hmcts.opal.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.AccountEnquiryDto;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.service.DefendantAccountService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class DefendantAccountControllerTest {

    @InjectMocks
    private DefendantAccountController defendantAccountController;

    @Mock
    private DefendantAccountService defendantAccountService;

    private ObjectMapper objectMapper;

    @Value("${TEST_URL:http://localhost:4550}")
    private String testUrl;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        RestAssured.baseURI = testUrl;
        RestAssured.useRelaxedHTTPSValidation();
    }

    @Test
    public void testGetDefendantAccount_Success() {
        // Arrange
        AccountEnquiryDto request = AccountEnquiryDto.builder().build();
        DefendantAccountEntity mockResponse = new DefendantAccountEntity();

        when(defendantAccountService.getDefendantAccount(any(AccountEnquiryDto.class))).thenReturn(mockResponse);

        // Act
        ResponseEntity<DefendantAccountEntity> responseEntity = defendantAccountController.getDefendantAccount(request);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(mockResponse, responseEntity.getBody());
        verify(defendantAccountService, times(1)).getDefendantAccount(any(
            AccountEnquiryDto.class));
    }

    @Test
    public void testGetDefendantAccount_NoContent() {
        // Arrange
        AccountEnquiryDto request = AccountEnquiryDto.builder().build();

        when(defendantAccountService.getDefendantAccount(any(AccountEnquiryDto.class))).thenReturn(null);

        // Act
        ResponseEntity<DefendantAccountEntity> responseEntity = defendantAccountController.getDefendantAccount(request);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
        verify(defendantAccountService, times(1)).getDefendantAccount(any(
            AccountEnquiryDto.class));
    }

    @Test
    public void testPutDefendantAccount_Success() {
        // Arrange
        DefendantAccountEntity requestEntity = new DefendantAccountEntity();
        DefendantAccountEntity mockResponse = new DefendantAccountEntity();

        when(defendantAccountService.putDefendantAccount(any(DefendantAccountEntity.class))).thenReturn(mockResponse);

        // Act
        ResponseEntity<DefendantAccountEntity> responseEntity = defendantAccountController.putDefendantAccount(
            requestEntity);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(mockResponse, responseEntity.getBody());
        verify(defendantAccountService, times(1)).putDefendantAccount(any(
            DefendantAccountEntity.class));
    }

    @Test
    public void testControllerModelEqualsAndHashCode() {
        // Arrange
        DefendantAccountController model1 = new DefendantAccountController();
        DefendantAccountController model2 = new DefendantAccountController();

        // Assert
        assertEquals(model1, model2);
        assertEquals(model1.hashCode(), model2.hashCode());
    }

    @Test
    public void testControllerModelToString() {
        // Arrange
        DefendantAccountController model = new DefendantAccountController();

        // Act
        String result = model.toString();

        // Assert
        assertNotNull(result);
    }

    @Test
    public void testSetAndGetServiceClass() {
        // Arrange
        DefendantAccountController model = new DefendantAccountController();

        // Act
        model.setDefendantAccountService(new DefendantAccountService());

        // Assert
        assertNotNull(model.getDefendantAccountService());
    }
}
