package uk.gov.hmcts.opal.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.opal.dto.AccountEnquiryDto;
import uk.gov.hmcts.opal.dto.AccountSearchDto;
import uk.gov.hmcts.opal.dto.AccountSearchResultsDto;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.service.legacy.dto.DefendantAccountSearchCriteria;
import uk.gov.hmcts.opal.service.legacy.dto.DefendantAccountsSearchResults;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LegacyDefendantAccountServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private LegacyDefendantAccountService legacyDefendantAccountService;

    @Test
    @SuppressWarnings("unchecked")
    void putDefendantAccount_SuccessfulResponse() throws Exception {
        // Arrange
        final DefendantAccountEntity inputAccountEntity = DefendantAccountServiceTest.buildDefendantAccountEntity();

        DefendantAccountEntity expectedAccountEntity = DefendantAccountServiceTest.buildDefendantAccountEntity();

        String jsonBody = ToJsonString.newObjectMapper().writeValueAsString(inputAccountEntity);

        ResponseEntity<String> successfulResponseEntity = new ResponseEntity<>(jsonBody, HttpStatus.OK);
        when(restTemplate.postForEntity(any(String.class), any(DefendantAccountEntity.class), any(Class.class)))
            .thenReturn(successfulResponseEntity);

        // Act
        DefendantAccountEntity resultPartyDto = legacyDefendantAccountService.putDefendantAccount(inputAccountEntity);

        // Assert
        assertEquals(expectedAccountEntity, resultPartyDto);
    }

    @Test
    @SuppressWarnings("unchecked")
    void putDefendantAccount_FailureBodyResponse() throws Exception {
        // Arrange
        final DefendantAccountEntity inputAccountEntity = DefendantAccountServiceTest.buildDefendantAccountEntity();

        ResponseEntity<String> unsuccessfulResponseEntity = new ResponseEntity<>(
            null, HttpStatus.OK);
        when(restTemplate.postForEntity(any(String.class), any(DefendantAccountEntity.class), any(Class.class)))
            .thenReturn(unsuccessfulResponseEntity);

        // Act
        DefendantAccountEntity resultPartyDto = legacyDefendantAccountService.putDefendantAccount(inputAccountEntity);

        // Assert

        assertNull(resultPartyDto);
    }

    @Test
    @SuppressWarnings("unchecked")
    void putDefendantAccount_FailureCodeResponse() throws Exception {
        // Arrange
        final DefendantAccountEntity inputAccountEntity = DefendantAccountServiceTest.buildDefendantAccountEntity();

        String jsonBody = ToJsonString.newObjectMapper().writeValueAsString(inputAccountEntity);

        ResponseEntity<String> unsuccessfulResponseEntity = new ResponseEntity<>(
            jsonBody, HttpStatus.INTERNAL_SERVER_ERROR);
        when(restTemplate.postForEntity(any(String.class), any(DefendantAccountEntity.class), any(Class.class)))
            .thenReturn(unsuccessfulResponseEntity);

        // Act
        DefendantAccountEntity resultPartyDto = legacyDefendantAccountService.putDefendantAccount(inputAccountEntity);

        // Assert

        assertNull(resultPartyDto);
    }

    @Test
    @SuppressWarnings("unchecked")
    void putDefendantAccount_ErrorResponse() throws Exception {
        // Arrange
        final DefendantAccountEntity inputAccountEntity = DefendantAccountServiceTest.buildDefendantAccountEntity();

        String jsonBody = createBrokenJson();

        ResponseEntity<String> unsuccessfulResponseEntity = new ResponseEntity<>(
            jsonBody, HttpStatus.OK);
        when(restTemplate.postForEntity(any(String.class), any(DefendantAccountEntity.class), any(Class.class)))
            .thenReturn(unsuccessfulResponseEntity);

        // Act
        DefendantAccountEntity resultPartyDto = legacyDefendantAccountService.putDefendantAccount(inputAccountEntity);

        // Assert

        assertNull(resultPartyDto);
    }

    @Test
    @SuppressWarnings("unchecked")
    void getParty_SuccessfulResponse() throws Exception {
        // Arrange
        final DefendantAccountEntity inputAccountEntity = DefendantAccountServiceTest.buildDefendantAccountEntity();

        DefendantAccountEntity expectedAccountEntity = DefendantAccountServiceTest.buildDefendantAccountEntity();

        String jsonBody = ToJsonString.newObjectMapper().writeValueAsString(inputAccountEntity);

        ResponseEntity<String> successfulResponseEntity = new ResponseEntity<>(jsonBody, HttpStatus.OK);
        when(restTemplate.getForEntity(any(String.class), any(Class.class), any(AccountEnquiryDto.class)))
            .thenReturn(successfulResponseEntity);

        // Act
        AccountEnquiryDto enquiry = AccountEnquiryDto.builder().build();
        DefendantAccountEntity resultAccountEntity = legacyDefendantAccountService.getDefendantAccount(enquiry);

        // Assert
        assertEquals(expectedAccountEntity, resultAccountEntity);
    }

    @Test
    @SuppressWarnings("unchecked")
    void getParty_FailureBodyResponse() throws Exception {
        // Arrange
        final DefendantAccountEntity inputAccountEntity = DefendantAccountServiceTest.buildDefendantAccountEntity();


        ResponseEntity<String> unsuccessfulResponseEntity = new ResponseEntity<>(
            null, HttpStatus.OK);
        when(restTemplate.getForEntity(any(String.class), any(Class.class), any(AccountEnquiryDto.class)))
            .thenReturn(unsuccessfulResponseEntity);

        // Act
        AccountEnquiryDto enquiry = AccountEnquiryDto.builder().build();
        DefendantAccountEntity resultAccountEntity = legacyDefendantAccountService.getDefendantAccount(enquiry);

        // Assert

        assertNull(resultAccountEntity);
    }

    @Test
    @SuppressWarnings("unchecked")
    void getParty_FailureCodeResponse() throws Exception {
        // Arrange
        final DefendantAccountEntity inputAccountEntity = DefendantAccountServiceTest.buildDefendantAccountEntity();


        String jsonBody = ToJsonString.newObjectMapper().writeValueAsString(inputAccountEntity);

        ResponseEntity<String> unsuccessfulResponseEntity = new ResponseEntity<>(
            jsonBody, HttpStatus.INTERNAL_SERVER_ERROR);
        when(restTemplate.getForEntity(any(String.class), any(Class.class), any(AccountEnquiryDto.class)))
            .thenReturn(unsuccessfulResponseEntity);

        // Act
        AccountEnquiryDto enquiry = AccountEnquiryDto.builder().build();
        DefendantAccountEntity resultAccountEntity = legacyDefendantAccountService.getDefendantAccount(enquiry);

        // Assert

        assertNull(resultAccountEntity);
    }

    @Test
    @SuppressWarnings("unchecked")
    void getParty_ErrorResponse() throws Exception {
        // Arrange

        String jsonBody = createBrokenJson();

        ResponseEntity<String> unsuccessfulResponseEntity = new ResponseEntity<>(
            jsonBody, HttpStatus.OK);
        when(restTemplate.getForEntity(any(String.class), any(Class.class), any(AccountEnquiryDto.class)))
            .thenReturn(unsuccessfulResponseEntity);

        // Act
        AccountEnquiryDto enquiry = AccountEnquiryDto.builder().build();
        DefendantAccountEntity resultAccountEntity = legacyDefendantAccountService.getDefendantAccount(enquiry);

        // Assert

        assertNull(resultAccountEntity);
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchForParty_SuccessfulResponse() throws Exception {
        // Arrange
        DefendantAccountsSearchResults resultsDto = DefendantAccountsSearchResults.builder()
            .totalCount(9L).build();
        String jsonBody = ToJsonString.newObjectMapper().writeValueAsString(resultsDto);

        ResponseEntity<String> successfulResponseEntity = new ResponseEntity<>(jsonBody, HttpStatus.OK);
        when(restTemplate.postForEntity(any(String.class), any(DefendantAccountSearchCriteria.class), any(Class.class)))
            .thenReturn(successfulResponseEntity);
        // Act
        AccountSearchResultsDto searchResultsDto = legacyDefendantAccountService
            .searchDefendantAccounts(AccountSearchDto.builder().build());

        // Assert
        assertEquals(9L, searchResultsDto.getTotalCount());
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
