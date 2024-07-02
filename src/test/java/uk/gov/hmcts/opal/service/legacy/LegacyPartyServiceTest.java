package uk.gov.hmcts.opal.service.legacy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.PartyDto;
import uk.gov.hmcts.opal.dto.search.PartySearchDto;
import uk.gov.hmcts.opal.entity.CourtEntity;
import uk.gov.hmcts.opal.entity.PartySummary;
import uk.gov.hmcts.opal.service.opal.PartyServiceTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
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

        final PartyDto expectedPartyDto = PartyServiceTest.buildPartyDto();

        String jsonBody = createXml();

        ResponseEntity<String> successfulResponseEntity = new ResponseEntity<>(jsonBody, HttpStatus.OK);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
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
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
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


        String jsonBody = createXml();

        ResponseEntity<String> unsuccessfulResponseEntity = new ResponseEntity<>(
            jsonBody, HttpStatus.INTERNAL_SERVER_ERROR);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
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


        String jsonBody = createBrokenXml();

        ResponseEntity<String> unsuccessfulResponseEntity = new ResponseEntity<>(
            jsonBody, HttpStatus.OK);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
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
        assertEquals(RuntimeException.class, cause.getClass());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getParty_SuccessfulResponse() throws Exception {
        // Arrange
        mockRestClientPost();
        final PartyDto inputPartyDto = new PartyDto();

        final PartyDto expectedPartyDto = PartyServiceTest.buildPartyDto();

        String jsonBody = createXml();

        ResponseEntity<String> successfulResponseEntity = new ResponseEntity<>(jsonBody, HttpStatus.OK);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
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
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
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

        String jsonBody = createXml();

        ResponseEntity<String> unsuccessfulResponseEntity = new ResponseEntity<>(
            jsonBody, HttpStatus.INTERNAL_SERVER_ERROR);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
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
        String jsonBody = createBrokenXml();

        ResponseEntity<String> unsuccessfulResponseEntity = new ResponseEntity<>(
            jsonBody, HttpStatus.OK);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
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
        assertEquals(RuntimeException.class, cause.getClass());
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

    @Test
    void testSearchCourts() {
        // Arrange

        CourtEntity courtEntity = CourtEntity.builder().build();

        // Act
        LegacyGatewayResponseException exception = assertThrows(
            LegacyGatewayResponseException.class,
            () -> legacyPartyService.searchParties(PartySearchDto.builder().build())
        );

        // Assert
        assertNotNull(exception);
        assertEquals(NOT_YET_IMPLEMENTED, exception.getMessage());

    }

    private static String createXml() {
        return """
            <party>
              <organisation>false</organisation>
              <surname>Smith</surname>
              <forenames>John James</forenames>
              <initials>JJ</initials>
              <title>Mr</title>
              <addressLine1>22 Acacia Avenue</addressLine1>
              <addressLine2>Hammersmith</addressLine2>
              <addressLine3>Birmingham</addressLine3>
              <addressLine4>Cornwall</addressLine4>
              <addressLine5>Scotland</addressLine5>
              <postcode>SN15 9TT</postcode>
              <accountType>TFO</accountType>
              <dateOfBirth>2001-08-16</dateOfBirth>
              <age>21</age>
              <niNumber>FF22446688</niNumber>
              <lastChangedDate>2023-12-05T15:45</lastChangedDate>
             </party>
            """;
    }

    private static String createBrokenXml() {
        return """
            <party>
              <organisation>false</organisation>
              <surname>Smith</surname>
              <forenames>John James</forenames>
              <initials>JJ</initials>
              <title>Mr</title>
              <FOOBAR 1>22 Acacia Avenue</FOOBAR 1>
              <FOOBAR 2>Hammersmith</FOOBAR 2>
              <FOOBAR 3>Birmingham</FOOBAR 3>
              <FOOBAR 4>Cornwall</FOOBAR 4>
              <FOOBAR 5>Scotland</FOOBAR 5>
              <postcode>SN15 9TT</postcode>
              <accountType>TFO</accountType>
              <dateOfBirth>2001-08-16</dateOfBirth>
              <age>21</age>
              <niNumber>FF22446688</niNumber>
              <lastChangedDate>2023-12-05T15:45</lastChangedDate>
             </party>
            """;
    }

}
