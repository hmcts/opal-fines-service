package uk.gov.hmcts.opal.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
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
import uk.gov.hmcts.opal.dto.legacy.DefendantAccountDto;
import uk.gov.hmcts.opal.dto.legacy.LegacyAccountDetailsRequestDto;
import uk.gov.hmcts.opal.dto.legacy.LegacyAccountDetailsResponseDto;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.service.legacy.dto.DefendantAccountSearchCriteria;
import uk.gov.hmcts.opal.service.legacy.dto.DefendantAccountsSearchResults;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;

import static org.junit.Assert.assertTrue;
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

    @Test
    void getAccountDetailsByDefendantAccountId_ValidateRequest() throws IOException, ProcessingException {
        //Arrange

        LegacyAccountDetailsRequestDto legacyAccountDetailsDto = LegacyAccountDetailsRequestDto.builder()
            .defendantAccountId(1L)
            .build();

        // Serialize the DTO to JSON using Jackson
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(legacyAccountDetailsDto);

        String content = Files.readString(
            Paths.get("src/test/resources/schemas/AccountDetails/of_f_get_defendant_account_in.json"),
            StandardCharsets.UTF_8);

        // Parse the JSON schema
        JsonSchemaFactory schemaFactory = JsonSchemaFactory.byDefault();
        JsonSchema schema = schemaFactory.getJsonSchema(JsonLoader.fromString(content));

        // Validate the serialized JSON against the schema
        assertTrue(schema.validInstance(JsonLoader.fromString(json)));
    }

    @Test
    void getAccountDetailsByDefendantAccountId_ValidateResponse() throws IOException, ProcessingException {

        DefendantAccountDto defendantAccountDto = buildDefendantAccountDto();

        LegacyAccountDetailsResponseDto legacyAccountDetailsDto = LegacyAccountDetailsResponseDto.builder()
            .defendantAccount(defendantAccountDto)
            .build();

        // Serialize the DTO to JSON using Jackson
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.registerModule(new JavaTimeModule());
        String json = objectMapper.writeValueAsString(legacyAccountDetailsDto);

        String content = Files.readString(
            Paths.get("src/test/resources/schemas/AccountDetails/of_f_get_defendant_account_out.json"),
            StandardCharsets.UTF_8);

        // Parse the JSON schema
        JsonSchemaFactory schemaFactory = JsonSchemaFactory.byDefault();
        JsonSchema schema = schemaFactory.getJsonSchema(JsonLoader.fromString(content));

        // Generate validation report
        ProcessingReport report = schema.validate(JsonLoader.fromString(json));

        // Validate the serialized JSON against the schema
        assertTrue(report.isSuccess());

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

    private DefendantAccountDto buildDefendantAccountDto() {

        return DefendantAccountDto.builder()
            .defendantAccountId(1L)
            .accountNumber("AA11")
            .amountPaid(BigDecimal.valueOf(100.00))
            .amountImposed(BigDecimal.valueOf(100.00))
            .accountBalance(BigDecimal.valueOf(100.00))
            .businessUnitId(200)
            .businessUnitName("A Business")
            .accountStatus("OPEN")
            .originatorName("Originator")
            .imposingCourtCode(10)
            .lastHearingDate("2020-01-01")
            .lastHearingCourtCode(10)
            .lastChangedDate(LocalDate.of(2012, 1,1))
            .lastMovementDate(LocalDate.of(2012, 1,1))
            .collectionOrder(true)
            .enforcingCourtCode(10)
            .lastEnforcement("ENF")
            .prosecutorCaseReference("1234")
            .build();
    }
}
