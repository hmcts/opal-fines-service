package uk.gov.hmcts.opal.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.opal.dto.AccountSearchDto;
import uk.gov.hmcts.opal.dto.PartyDto;
import uk.gov.hmcts.opal.entity.PartySummary;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LegacyPartyServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private Logger log;

    @InjectMocks
    private LegacyPartyService legacyPartyService;


    @Test
    @SuppressWarnings("unchecked")
    void saveParty_SuccessfulResponse() throws Exception {
        // Arrange
        final PartyDto inputPartyDto = new PartyDto();

        PartyDto expectedPartyDto = PartyServiceTest.buildPartyDto();

        String jsonBody = createJsonBody1();

        ResponseEntity<String> successfulResponseEntity = new ResponseEntity<>(jsonBody, HttpStatus.OK);
        when(restTemplate.postForEntity(any(String.class), any(PartyDto.class), any(Class.class)))
            .thenReturn(successfulResponseEntity);

        // Act
        PartyDto resultPartyDto = legacyPartyService.saveParty(inputPartyDto);

        // Assert
        assertEquals(expectedPartyDto.toPrettyJsonString(), resultPartyDto.toPrettyJsonString());
    }

    @Test
    @SuppressWarnings("unchecked")
    void saveParty_FailureBodyResponse() throws Exception {
        // Arrange
        final PartyDto inputPartyDto = new PartyDto();


        ResponseEntity<String> unsuccessfulResponseEntity = new ResponseEntity<>(
            null, HttpStatus.OK);
        when(restTemplate.postForEntity(any(String.class), any(PartyDto.class), any(Class.class)))
            .thenReturn(unsuccessfulResponseEntity);

        // Act
        PartyDto resultPartyDto = legacyPartyService.saveParty(inputPartyDto);

        // Assert

        assertNull(resultPartyDto);
    }

    @Test
    @SuppressWarnings("unchecked")
    void saveParty_FailureCodeResponse() throws Exception {
        // Arrange
        final PartyDto inputPartyDto = new PartyDto();


        String jsonBody = createJsonBody1();

        ResponseEntity<String> unsuccessfulResponseEntity = new ResponseEntity<>(
            jsonBody, HttpStatus.INTERNAL_SERVER_ERROR);
        when(restTemplate.postForEntity(any(String.class), any(PartyDto.class), any(Class.class)))
            .thenReturn(unsuccessfulResponseEntity);

        // Act
        PartyDto resultPartyDto = legacyPartyService.saveParty(inputPartyDto);

        // Assert

        assertNull(resultPartyDto);
    }

    @Test
    @SuppressWarnings("unchecked")
    void saveParty_ErrorResponse() throws Exception {
        // Arrange
        final PartyDto inputPartyDto = new PartyDto();


        String jsonBody = createBrokenJson();

        ResponseEntity<String> unsuccessfulResponseEntity = new ResponseEntity<>(
            jsonBody, HttpStatus.OK);
        when(restTemplate.postForEntity(any(String.class), any(PartyDto.class), any(Class.class)))
            .thenReturn(unsuccessfulResponseEntity);

        // Act
        PartyDto resultPartyDto = legacyPartyService.saveParty(inputPartyDto);

        // Assert

        assertNull(resultPartyDto);
    }

    @Test
    @SuppressWarnings("unchecked")
    void getParty_SuccessfulResponse() throws Exception {
        // Arrange
        final PartyDto inputPartyDto = new PartyDto();

        PartyDto expectedPartyDto = PartyServiceTest.buildPartyDto();

        String jsonBody = createJsonBody1();

        ResponseEntity<String> successfulResponseEntity = new ResponseEntity<>(jsonBody, HttpStatus.OK);
        when(restTemplate.getForEntity(any(String.class), any(Class.class), any(Long.class)))
            .thenReturn(successfulResponseEntity);

        // Act
        PartyDto resultPartyDto = legacyPartyService.getParty(1L);

        // Assert
        assertEquals(expectedPartyDto.toPrettyJsonString(), resultPartyDto.toPrettyJsonString());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getParty_FailureBodyResponse() throws Exception {
        // Arrange
        final PartyDto inputPartyDto = new PartyDto();


        ResponseEntity<String> unsuccessfulResponseEntity = new ResponseEntity<>(
            null, HttpStatus.OK);
        when(restTemplate.getForEntity(any(String.class), any(Class.class), any(Long.class)))
            .thenReturn(unsuccessfulResponseEntity);

        // Act
        PartyDto resultPartyDto = legacyPartyService.getParty(1L);

        // Assert

        assertNull(resultPartyDto);
    }

    @Test
    @SuppressWarnings("unchecked")
    void getParty_FailureCodeResponse() throws Exception {
        // Arrange
        final PartyDto inputPartyDto = new PartyDto();


        String jsonBody = createJsonBody1();

        ResponseEntity<String> unsuccessfulResponseEntity = new ResponseEntity<>(
            jsonBody, HttpStatus.INTERNAL_SERVER_ERROR);
        when(restTemplate.getForEntity(any(String.class), any(Class.class), any(Long.class)))
            .thenReturn(unsuccessfulResponseEntity);

        // Act
        PartyDto resultPartyDto = legacyPartyService.getParty(1L);

        // Assert

        assertNull(resultPartyDto);
    }

    @Test
    @SuppressWarnings("unchecked")
    void getParty_ErrorResponse() throws Exception {
        // Arrange

        String jsonBody = createBrokenJson();

        ResponseEntity<String> unsuccessfulResponseEntity = new ResponseEntity<>(
            jsonBody, HttpStatus.OK);
        when(restTemplate.getForEntity(any(String.class), any(Class.class), any(Long.class)))
            .thenReturn(unsuccessfulResponseEntity);

        // Act
        PartyDto resultPartyDto = legacyPartyService.getParty(1L);

        // Assert

        assertNull(resultPartyDto);
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchForParty_SuccessfulResponse() throws Exception {
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
