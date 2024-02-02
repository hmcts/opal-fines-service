package uk.gov.hmcts.opal.service.legacy;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.AccountSearchDto;
import uk.gov.hmcts.opal.dto.PartyDto;
import uk.gov.hmcts.opal.entity.PartySummary;
import uk.gov.hmcts.opal.service.opal.PartyServiceTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LegacyPartyServiceTest extends LegacyTestsBase {

    @Mock
    private Logger log;

    @InjectMocks
    private LegacyPartyService legacyPartyService;

    @Test
    @SuppressWarnings("unchecked")
    void saveParty_SuccessfulResponse() throws Exception {
        // Arrange
        mockRestClientPost();
        final PartyDto inputPartyDto = new PartyDto();

        PartyDto expectedPartyDto = PartyServiceTest.buildPartyDto();

        String jsonBody = createJsonBody1();

        ResponseEntity<String> successfulResponseEntity = new ResponseEntity<>(jsonBody, HttpStatus.OK);
        when(requestBodySpec.body(any(PartyDto.class))).thenReturn(requestBodySpec);
        when(responseSpec.toEntity(any(Class.class))).thenReturn(successfulResponseEntity);

        // Act
        PartyDto resultPartyDto = legacyPartyService.saveParty(inputPartyDto);

        // Assert
        assertEquals(expectedPartyDto.toPrettyJsonString(), resultPartyDto.toPrettyJsonString());
    }

    @Test
    @SuppressWarnings("unchecked")
    void saveParty_FailureBodyResponse() {
        // Arrange
        mockRestClientPost();
        final PartyDto inputPartyDto = new PartyDto();


        ResponseEntity<String> unsuccessfulResponseEntity = new ResponseEntity<>(
            null, HttpStatus.OK);
        when(requestBodySpec.body(any(PartyDto.class))).thenReturn(requestBodySpec);
        when(responseSpec.toEntity(any(Class.class))).thenReturn(unsuccessfulResponseEntity);

        // Act
        LegacyGatewayResponseException lgre = assertThrows(
            LegacyGatewayResponseException.class,
            () -> legacyPartyService.saveParty(inputPartyDto)
        );

        // Assert

        assertNotNull(lgre);
        assertEquals(
            "Received an empty body in the response from the Legacy Gateway.",
            lgre.getMessage()
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void saveParty_FailureCodeResponse() {
        // Arrange
        mockRestClientPost();
        final PartyDto inputPartyDto = new PartyDto();


        String jsonBody = createJsonBody1();

        ResponseEntity<String> unsuccessfulResponseEntity = new ResponseEntity<>(
            jsonBody, HttpStatus.INTERNAL_SERVER_ERROR);
        when(requestBodySpec.body(any(PartyDto.class))).thenReturn(requestBodySpec);
        when(responseSpec.toEntity(any(Class.class))).thenReturn(unsuccessfulResponseEntity);

        // Act
        LegacyGatewayResponseException lgre = assertThrows(
            LegacyGatewayResponseException.class,
            () -> legacyPartyService.saveParty(inputPartyDto)
        );

        // Assert

        assertNotNull(lgre);
        assertEquals(
            "Received a non-2xx response from the Legacy Gateway: 500 INTERNAL_SERVER_ERROR",
            lgre.getMessage()
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void saveParty_ErrorResponse() {
        // Arrange
        mockRestClientPost();
        final PartyDto inputPartyDto = new PartyDto();


        String jsonBody = createBrokenJson();

        ResponseEntity<String> unsuccessfulResponseEntity = new ResponseEntity<>(
            jsonBody, HttpStatus.OK);
        when(requestBodySpec.body(any(PartyDto.class))).thenReturn(requestBodySpec);
        when(responseSpec.toEntity(any(Class.class))).thenReturn(unsuccessfulResponseEntity);

        // Act
        LegacyGatewayResponseException lgre = assertThrows(
            LegacyGatewayResponseException.class,
            () -> legacyPartyService.saveParty(inputPartyDto)
        );

        // Assert

        assertNotNull(lgre);
        Throwable cause = lgre.getCause();
        assertNotNull(cause);
        assertEquals(UnrecognizedPropertyException.class, cause.getClass());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getParty_SuccessfulResponse() throws Exception {
        // Arrange
        mockRestClientPost();
        final PartyDto inputPartyDto = new PartyDto();

        PartyDto expectedPartyDto = PartyServiceTest.buildPartyDto();

        String jsonBody = createJsonBody1();

        ResponseEntity<String> successfulResponseEntity = new ResponseEntity<>(jsonBody, HttpStatus.OK);
        when(requestBodySpec.body(any(String.class))).thenReturn(requestBodySpec);
        when(responseSpec.toEntity(any(Class.class))).thenReturn(successfulResponseEntity);

        // Act
        PartyDto resultPartyDto = legacyPartyService.getParty(1L);

        // Assert
        assertEquals(expectedPartyDto.toPrettyJsonString(), resultPartyDto.toPrettyJsonString());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getParty_FailureBodyResponse() {
        // Arrange
        mockRestClientPost();
        final PartyDto inputPartyDto = new PartyDto();


        ResponseEntity<String> unsuccessfulResponseEntity = new ResponseEntity<>(
            null, HttpStatus.OK);
        when(requestBodySpec.body(any(String.class))).thenReturn(requestBodySpec);
        when(responseSpec.toEntity(any(Class.class))).thenReturn(unsuccessfulResponseEntity);

        // Act
        LegacyGatewayResponseException lgre = assertThrows(
            LegacyGatewayResponseException.class,
            () -> legacyPartyService.getParty(1L)
        );

        // Assert

        assertNotNull(lgre);
        assertEquals(
            "Received an empty body in the response from the Legacy Gateway.",
            lgre.getMessage()
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void getParty_FailureCodeResponse() {
        // Arrange
        mockRestClientPost();

        String jsonBody = createJsonBody1();

        ResponseEntity<String> unsuccessfulResponseEntity = new ResponseEntity<>(
            jsonBody, HttpStatus.INTERNAL_SERVER_ERROR);
        when(requestBodySpec.body(any(String.class))).thenReturn(requestBodySpec);
        when(responseSpec.toEntity(any(Class.class))).thenReturn(unsuccessfulResponseEntity);

        // Act
        LegacyGatewayResponseException lgre = assertThrows(
            LegacyGatewayResponseException.class,
            () -> legacyPartyService.getParty(1L)
        );

        // Assert

        assertNotNull(lgre);
        assertEquals(
            "Received a non-2xx response from the Legacy Gateway: 500 INTERNAL_SERVER_ERROR",
            lgre.getMessage()
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void getParty_ErrorResponse() {
        // Arrange
        mockRestClientPost();
        String jsonBody = createBrokenJson();

        ResponseEntity<String> unsuccessfulResponseEntity = new ResponseEntity<>(
            jsonBody, HttpStatus.OK);
        when(requestBodySpec.body(any(String.class))).thenReturn(requestBodySpec);
        when(responseSpec.toEntity(any(Class.class))).thenReturn(unsuccessfulResponseEntity);

        // Act
        LegacyGatewayResponseException lgre = assertThrows(
            LegacyGatewayResponseException.class,
            () -> legacyPartyService.getParty(1L)
        );

        // Assert

        assertNotNull(lgre);
        Throwable cause = lgre.getCause();
        assertNotNull(cause);
        assertEquals(UnrecognizedPropertyException.class, cause.getClass());
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchForParty_SuccessfulResponse() {
        // Arrange

        // Act
        List<PartySummary> resultPartyDto = legacyPartyService.searchForParty(AccountSearchDto.builder().build());

        // Assert
        assertEquals(0, resultPartyDto.size());
    }

    private static String createJsonBody1() {
        return """
            {
              "organisation" : false,
              "organisationName" : null,
              "surname" : "Smith",
              "forenames" : "John James",
              "initials" : "JJ",
              "title" : "Mr",
              "addressLine1" : "22 Acacia Avenue",
              "addressLine2" : "Hammersmith",
              "addressLine3" : "Birmingham",
              "addressLine4" : "Cornwall",
              "addressLine5" : "Scotland",
              "postcode" : "SN15 9TT",
              "accountType" : "TFO",
              "dateOfBirth" : [ 2001, 8, 16 ],
              "age" : 21,
              "niNumber" : "FF22446688",
              "lastChangedDate" : [ 2023, 12, 5, 15, 45 ]
            }
            """;
    }

    private static String createBrokenJson() {
        return """
            {
              "organisation" : false,
              "organisationName" : null,
              "surname" : "Smith",
              "forenames" : "John James",
              "initials" : "JJ",
              "title" : "Mr",
              "FOOBAR 1" : "22 Acacia Avenue",
              "FOOBAR 2" : "Hammersmith",
              "FOOBAR 3" : "Birmingham",
              "FOOBAR 4" : "Cornwall",
              "FOOBAR 5" : "Scotland",
              "postcode" : "SN15 9TT",
              "accountType" : "TFO",
              "dateOfBirth" : [ 2001, 8, 16 ],
              "age" : 21,
              "niNumber" : "FF22446688",
              "lastChangedDate" : [ 2023, 12, 5, 15, 45 ]
            }
            """;
    }

}
