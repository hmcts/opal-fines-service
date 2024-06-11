package uk.gov.hmcts.opal.service.legacy;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.AccountDetailsDto;
import uk.gov.hmcts.opal.dto.AccountEnquiryDto;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.AccountSearchResultsDto;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.dto.legacy.AccountActivitiesDto;
import uk.gov.hmcts.opal.dto.legacy.AccountActivityDto;
import uk.gov.hmcts.opal.dto.legacy.DefendantAccountDto;
import uk.gov.hmcts.opal.dto.legacy.DefendantAccountSearchCriteria;
import uk.gov.hmcts.opal.dto.legacy.DefendantAccountSearchResult;
import uk.gov.hmcts.opal.dto.legacy.DefendantAccountsSearchResults;
import uk.gov.hmcts.opal.dto.legacy.ImpositionDto;
import uk.gov.hmcts.opal.dto.legacy.ImpositionsDto;
import uk.gov.hmcts.opal.dto.legacy.LegacyAccountDetailsRequestDto;
import uk.gov.hmcts.opal.dto.legacy.LegacyAccountDetailsResponseDto;
import uk.gov.hmcts.opal.dto.legacy.PartiesDto;
import uk.gov.hmcts.opal.dto.legacy.PartyDto;
import uk.gov.hmcts.opal.dto.legacy.PaymentTermsDto;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.service.opal.DefendantAccountServiceTest;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LegacyDefendantAccountServiceTest extends LegacyTestsBase {

    @InjectMocks
    private LegacyDefendantAccountService legacyDefendantAccountService;

    @Test
    @SuppressWarnings("unchecked")
    void putDefendantAccount_SuccessfulResponse() throws Exception {

        // Arrange
        mockRestClientPost();

        final DefendantAccountEntity inputAccountEntity = DefendantAccountServiceTest.buildDefendantAccountEntity();

        final DefendantAccountEntity expectedAccountEntity = DefendantAccountServiceTest.buildDefendantAccountEntity();

        String xml = marshalXmlString(inputAccountEntity, DefendantAccountEntity.class);

        ResponseEntity<String> successfulResponseEntity = new ResponseEntity<>(xml, HttpStatus.OK);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(DefendantAccountEntity.class))).thenReturn(requestBodySpec);
        when(responseSpec.toEntity(any(Class.class))).thenReturn(successfulResponseEntity);

        // Act
        DefendantAccountEntity resultPartyDto = legacyDefendantAccountService.putDefendantAccount(inputAccountEntity);

        // Assert
        assertEquals(expectedAccountEntity, resultPartyDto);
    }

    @Test
    @SuppressWarnings("unchecked")
    void putDefendantAccount_FailureBodyResponse() {
        // Arrange
        mockRestClientPost();
        final DefendantAccountEntity inputAccountEntity = DefendantAccountServiceTest.buildDefendantAccountEntity();

        ResponseEntity<String> unsuccessfulResponseEntity = new ResponseEntity<>(
            null, HttpStatus.OK);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(DefendantAccountEntity.class))).thenReturn(requestBodySpec);
        when(responseSpec.toEntity(any(Class.class))).thenReturn(unsuccessfulResponseEntity);

        // Act
        LegacyGatewayResponseException lgre = assertThrows(
            LegacyGatewayResponseException.class,
            () -> legacyDefendantAccountService.putDefendantAccount(inputAccountEntity)
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
    void putDefendantAccount_FailureCodeResponse() throws Exception {
        // Arrange
        mockRestClientPost();
        final DefendantAccountEntity inputAccountEntity = DefendantAccountServiceTest.buildDefendantAccountEntity();

        String xml = marshalXmlString(inputAccountEntity, DefendantAccountEntity.class);

        ResponseEntity<String> unsuccessfulResponseEntity = new ResponseEntity<>(
            xml, HttpStatus.INTERNAL_SERVER_ERROR);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(DefendantAccountEntity.class))).thenReturn(requestBodySpec);
        when(responseSpec.toEntity(any(Class.class))).thenReturn(unsuccessfulResponseEntity);

        // Act
        LegacyGatewayResponseException lgre = assertThrows(
            LegacyGatewayResponseException.class,
            () -> legacyDefendantAccountService.putDefendantAccount(inputAccountEntity)
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
    void putDefendantAccount_ErrorResponse() {
        // Arrange
        mockRestClientPost();
        final DefendantAccountEntity inputAccountEntity = DefendantAccountServiceTest.buildDefendantAccountEntity();

        String jsonBody = createBrokenJson();

        ResponseEntity<String> unsuccessfulResponseEntity = new ResponseEntity<>(
            jsonBody, HttpStatus.OK);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(DefendantAccountEntity.class))).thenReturn(requestBodySpec);
        when(responseSpec.toEntity(any(Class.class))).thenReturn(unsuccessfulResponseEntity);

        // Act
        LegacyGatewayResponseException lgre = assertThrows(
            LegacyGatewayResponseException.class,
            () -> legacyDefendantAccountService.putDefendantAccount(inputAccountEntity)
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
        final DefendantAccountEntity inputAccountEntity = DefendantAccountServiceTest.buildDefendantAccountEntity();

        final DefendantAccountEntity expectedAccountEntity = DefendantAccountServiceTest.buildDefendantAccountEntity();

        String xml = marshalXmlString(inputAccountEntity, DefendantAccountEntity.class);

        ResponseEntity<String> successfulResponseEntity = new ResponseEntity<>(xml, HttpStatus.OK);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(AccountEnquiryDto.class))).thenReturn(requestBodySpec);
        when(responseSpec.toEntity(any(Class.class))).thenReturn(successfulResponseEntity);


        // Act
        AccountEnquiryDto enquiry = AccountEnquiryDto.builder().build();
        DefendantAccountEntity resultAccountEntity = legacyDefendantAccountService.getDefendantAccount(enquiry);

        // Assert
        assertEquals(expectedAccountEntity, resultAccountEntity);
    }

    @Test
    @SuppressWarnings("unchecked")
    void getParty_FailureBodyResponse() {
        // Arrange
        mockRestClientPost();
        final DefendantAccountEntity inputAccountEntity = DefendantAccountServiceTest.buildDefendantAccountEntity();

        ResponseEntity<String> unsuccessfulResponseEntity = new ResponseEntity<>(
            null, HttpStatus.OK);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(AccountEnquiryDto.class))).thenReturn(requestBodySpec);
        when(responseSpec.toEntity(any(Class.class))).thenReturn(unsuccessfulResponseEntity);
        // Act
        AccountEnquiryDto enquiry = AccountEnquiryDto.builder().build();

        LegacyGatewayResponseException lgre = assertThrows(
            LegacyGatewayResponseException.class,
            () -> legacyDefendantAccountService.getDefendantAccount(enquiry)
        );

        // Assert

        assertNotNull(lgre);
        assertEquals("Received an empty body in the response from the Legacy Gateway.", lgre.getMessage());
        // Assert

    }

    @Test
    @SuppressWarnings("unchecked")
    void getParty_FailureCodeResponse() throws Exception {
        // Arrange
        mockRestClientPost();
        final DefendantAccountEntity inputAccountEntity = DefendantAccountServiceTest.buildDefendantAccountEntity();


        String xml = marshalXmlString(inputAccountEntity, DefendantAccountEntity.class);

        ResponseEntity<String> unsuccessfulResponseEntity = new ResponseEntity<>(
            xml, HttpStatus.INTERNAL_SERVER_ERROR);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(AccountEnquiryDto.class))).thenReturn(requestBodySpec);
        when(responseSpec.toEntity(any(Class.class))).thenReturn(unsuccessfulResponseEntity);

        // Act
        AccountEnquiryDto enquiry = AccountEnquiryDto.builder().build();

        LegacyGatewayResponseException lgre = assertThrows(
            LegacyGatewayResponseException.class,
            () -> legacyDefendantAccountService.getDefendantAccount(enquiry)
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
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(AccountEnquiryDto.class))).thenReturn(requestBodySpec);
        when(responseSpec.toEntity(any(Class.class))).thenReturn(unsuccessfulResponseEntity);

        // Act
        AccountEnquiryDto enquiry = AccountEnquiryDto.builder().build();

        LegacyGatewayResponseException lgre = assertThrows(
            LegacyGatewayResponseException.class,
            () -> legacyDefendantAccountService.getDefendantAccount(enquiry)
        );

        // Assert
        assertNotNull(lgre);
        Throwable cause = lgre.getCause();
        assertNotNull(cause);
        assertEquals(RuntimeException.class, cause.getClass());
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
            StandardCharsets.UTF_8
        );

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
            StandardCharsets.UTF_8
        );

        // Parse the JSON schema
        JsonSchemaFactory schemaFactory = JsonSchemaFactory.byDefault();
        JsonSchema schema = schemaFactory.getJsonSchema(JsonLoader.fromString(content));

        // Generate validation report
        ProcessingReport report = schema.validate(JsonLoader.fromString(json));

        // Validate the serialized JSON against the schema
        assertTrue(report.isSuccess());

    }

    @SneakyThrows
    @Test
    @SuppressWarnings("unchecked")
    void getAccountDetailsByDefendantAccountId_Success() {

        // Arrange
        mockRestClientPost();

        String xml = marshalXmlString(buildLegacyAccountDto(), LegacyAccountDetailsResponseDto.class);

        ResponseEntity<String> successfulResponseEntity = new ResponseEntity<>(xml, HttpStatus.OK);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(LegacyAccountDetailsRequestDto.class))).thenReturn(requestBodySpec);
        when(responseSpec.toEntity(any(Class.class))).thenReturn(successfulResponseEntity);

        // Act
        AccountDetailsDto detailsResponseDto = legacyDefendantAccountService
            .getAccountDetailsByDefendantAccountId(123L);

        // Assert
        assertEquals(buildAccountDetailsDto(), detailsResponseDto);
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchForDefendantAccounts_SuccessfulResponse() throws Exception {

        mockRestClientPost();

        // Arrange
        DefendantAccountsSearchResults resultsDto = DefendantAccountsSearchResults.builder()
            .totalCount(9L).build();
        String xml = marshalXmlString(resultsDto, DefendantAccountsSearchResults.class);

        ResponseEntity<String> successfulResponseEntity = new ResponseEntity<>(xml, HttpStatus.OK);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(DefendantAccountSearchCriteria.class))).thenReturn(requestBodySpec);
        when(responseSpec.toEntity(any(Class.class))).thenReturn(successfulResponseEntity);

        // Act
        AccountSearchResultsDto searchResultsDto = legacyDefendantAccountService
            .searchDefendantAccounts(AccountSearchDto.builder().build());

        // Assert
        assertEquals(9L, searchResultsDto.getTotalCount());
    }

    @Test
    void searchForDefendantAccounts_ValidateRequest() throws IOException, ProcessingException {

        DefendantAccountSearchCriteria legacyAccountSearchCriteria = constructDefendantAccountSearchCriteria();

        // Serialize the DTO to JSON using Jackson
        String json = ToJsonString.getObjectMapper().writeValueAsString(legacyAccountSearchCriteria);

        String content = Files.readString(
            Paths.get("src/test/resources/schemas/AccountSearch/of_f_search_defendant_accounts_in.json"),
            StandardCharsets.UTF_8
        );

        // Parse the JSON schema
        JsonSchemaFactory schemaFactory = JsonSchemaFactory.byDefault();
        JsonSchema schema = schemaFactory.getJsonSchema(JsonLoader.fromString(content));

        // Validate the serialized JSON against the schema
        assertTrue(schema.validInstance(JsonLoader.fromString(json)));
    }

    @Test
    void searchForDefendantAccounts_ValidateResponse() throws IOException, ProcessingException {

        DefendantAccountsSearchResults legacyAccountsSearchResults = DefendantAccountsSearchResults.builder()
            .totalCount(1L)
            .defendantAccountsSearchResult(List.of(constructDefendantAccountSearchResult()))
            .build();

        // Serialize the DTO to JSON using Jackson
        ObjectMapper objectMapper = ToJsonString.getObjectMapper();
        String json = objectMapper.writeValueAsString(legacyAccountsSearchResults);

        String content = Files.readString(
            Paths.get("src/test/resources/schemas/AccountSearch/of_f_search_defendant_accounts_out.json"),
            StandardCharsets.UTF_8
        );

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
            .defendantAccountId(1000L)
            .parties(buildPartiesDto())
            .paymentTerms(buildPaymentTermsDto())
            .accountNumber("100")
            .amountPaid(BigDecimal.valueOf(100.00))
            .amountImposed(BigDecimal.valueOf(200.00))
            .accountBalance(BigDecimal.valueOf(100.00))
            .businessUnitId(200)
            .businessUnitName("CT")
            .accountStatus("OPEN")
            .originatorName("Originator")
            .imposingCourtCode(10)
            .lastHearingDate("2012-01-01")
            .lastHearingCourtCode(1212)
            .lastChangedDate(LocalDate.of(2012, 1, 1))
            .lastMovementDate(LocalDate.of(2012, 1, 1))
            .imposedHearingDate(LocalDate.of(2012, 1, 1))
            .enfOverrideResultId("OVER")
            .collectionOrder(true)
            .enforcingCourtCode(1)
            .enfOverrideEnforcerCode((short) 123)
            .lastEnforcement("ENF")
            .prosecutorCaseReference("123456")
            .accountComments("Comment1")
            .accountActivities(buildAccountActivitiesDto())
            .impositions(buildImpositionsDto())
            .build();
    }

    private PartiesDto buildPartiesDto() {

        return PartiesDto.builder()
            .party(List.of(buildPartyDto()))
            .build();
    }

    private PartyDto buildPartyDto() {

        return PartyDto.builder()
            .partyId(1)
            .debtor(true)
            .associationType("A_type")
            .addressLine1("1 High Street")
            .addressLine2("Westminster")
            .addressLine3("London")
            .postcode("W1 1AA")
            .fullName("Mr John Smith")
            .organisation(false)
            .birthDate(LocalDate.of(1979, 12, 12))
            .lastChangedDate("2020-02-02")
            .build();
    }

    private PaymentTermsDto buildPaymentTermsDto() {

        return PaymentTermsDto.builder()
            .termsTypeCode("I")
            .instalmentAmount(BigDecimal.valueOf(100.00))
            .instalmentPeriod("PCM")
            .termsDate(LocalDate.of(2012, 1, 1))
            .jailDays(10)
            .instalmentLumpSum(BigDecimal.valueOf(100.00))
            .wording("wording")
            .build();
    }

    private AccountActivitiesDto buildAccountActivitiesDto() {

        return AccountActivitiesDto.builder()
            .accountActivity(List.of(buildAccountActivityDto(), buildAccountActivityDtoOlder()))
            .build();
    }

    private AccountActivityDto buildAccountActivityDto() {

        return AccountActivityDto.builder()
            .activityId(1)
            .activityText("Activity")
            .activityType("Activity")
            .activityTypeCode("AA")
            .postedDate(LocalDateTime.of(2021, 1, 1, 21, 0, 0))
            .build();
    }

    private ImpositionsDto buildImpositionsDto() {

        return ImpositionsDto.builder()
            .imposition(List.of(buildImpositionDto()))
            .build();
    }

    private ImpositionDto buildImpositionDto() {

        return ImpositionDto.builder()
            .creditorAccountNumber("123")
            .creditorName("John")
            .imposedAmount(1000.00)
            .imposedDate("2020-01-01")
            .imposingCourtCode(12)
            .impositionId(1)
            .offenceTitle("A title")
            .paidAmount(100.00)
            .postedDate("2020-01-01")
            .resultId("1")
            .build();
    }

    private AccountActivityDto buildAccountActivityDtoOlder() {

        return AccountActivityDto.builder()
            .activityId(2)
            .activityText("Activity OLD")
            .activityType("Activity")
            .activityTypeCode("AA")
            .postedDate(LocalDateTime.of(2020, 1, 1, 21, 0, 0))
            .build();
    }

    private LegacyAccountDetailsResponseDto buildLegacyAccountDto() {

        return LegacyAccountDetailsResponseDto.builder()
            .defendantAccount(buildDefendantAccountDto())
            .build();
    }

    private AccountDetailsDto buildAccountDetailsDto() {

        return DefendantAccountServiceTest.buildAccountDetailsDto();
    }

    private DefendantAccountSearchCriteria constructDefendantAccountSearchCriteria() {
        return DefendantAccountSearchCriteria.builder()
            .accountNumber("accountNo")
            .addressLine1("Glasgow")
            .firstRowNumber(4)
            .lastRowNumber(44)
            .surname("Smith")
            .forenames("John")
            .initials("D")
            .birthDate("1977-06-26")
            .nationalInsuranceNumber("XX123456C")
            .build();
    }

    public static DefendantAccountSearchResult constructDefendantAccountSearchResult() {
        return DefendantAccountSearchResult.builder()
            .accountNumber("accountNo")
            .defendantAccountId(12345L)
            .surname("Smith")
            .forenames("John")
            .title("Mr")
            .birthDate("1977-06-26")
            .addressLine1("Scotland")
            .accountBalance(BigDecimal.valueOf(1000))
            .businessUnitId(9)
            .businessUnitName("Cardiff")
            .build();
    }
}
