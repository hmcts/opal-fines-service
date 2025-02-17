package uk.gov.hmcts.opal.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.UnexpectedRollbackException;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.controllers.util.UserStateUtil;
import uk.gov.hmcts.opal.dto.AddDraftAccountRequestDto;
import uk.gov.hmcts.opal.dto.DraftAccountResponseDto;
import uk.gov.hmcts.opal.dto.DraftAccountSummaryDto;
import uk.gov.hmcts.opal.dto.DraftAccountsResponseDto;
import uk.gov.hmcts.opal.dto.search.DraftAccountSearchDto;
import uk.gov.hmcts.opal.entity.DraftAccountEntity;
import uk.gov.hmcts.opal.entity.DraftAccountStatus;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnit;
import uk.gov.hmcts.opal.service.opal.DraftAccountService;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;
import uk.gov.hmcts.opal.service.opal.UserStateService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.opal.util.DateTimeUtils.toUtcDateTime;

@ExtendWith(MockitoExtension.class)
class DraftAccountControllerTest {

    static final String BEARER_TOKEN = "Bearer a_token_here";

    private static final short BU_ID = (short)1;

    @Mock
    private DraftAccountService draftAccountService;

    @Mock
    private UserStateService userStateService;

    @Spy
    private JsonSchemaValidationService jsonSchemaValidationService;

    @InjectMocks
    private DraftAccountController draftAccountController;

    @Test
    void testGetDraftAccount_Success() {
        // Arrange
        DraftAccountEntity entity = DraftAccountEntity.builder()
            .businessUnit(BusinessUnit.Lite.builder().businessUnitId(BU_ID).build())
            .build();

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(UserStateUtil.allPermissionsUser());
        when(draftAccountService.getDraftAccount(any(Long.class))).thenReturn(entity);

        // Act
        ResponseEntity<DraftAccountResponseDto> response = draftAccountController
            .getDraftAccountById(1L, BEARER_TOKEN);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(toGetDto(entity), response.getBody());
        verify(draftAccountService, times(1)).getDraftAccount(any(Long.class));
    }


    @Test
    void testGetDraftAccounts_Success() {
        // Arrange
        DraftAccountEntity entity = DraftAccountEntity.builder()
            .businessUnit(BusinessUnit.Lite.builder().businessUnitId(BU_ID).build())
            .build();

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(new UserState.DeveloperUserState());
        when(draftAccountService.getDraftAccounts(any(), any(), any(), any())).thenReturn(List.of(entity));

        // Act
        ResponseEntity<DraftAccountsResponseDto> response = draftAccountController
            .getDraftAccountSummaries(Optional.of(List.of(BU_ID)),
                                      Optional.of(List.of(DraftAccountStatus.PENDING)),
                                      Optional.of(List.of()),
                                      Optional.of(List.of()), BEARER_TOKEN);
        DraftAccountsResponseDto dto = response.getBody();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, dto.getCount());
        assertEquals(toSummaryDto(entity), dto.getSummaries().get(0));
        verify(draftAccountService, times(1)).getDraftAccounts(any(), any(), any(), any());
    }

    @Test
    void testSearchDraftAccounts_Success() {
        // Arrange
        DraftAccountEntity entity = DraftAccountEntity.builder().build();
        List<DraftAccountEntity> draftAccountList = List.of(entity);

        when(draftAccountService.searchDraftAccounts(any())).thenReturn(draftAccountList);

        // Act
        DraftAccountSearchDto searchDto = DraftAccountSearchDto.builder().build();
        ResponseEntity<List<DraftAccountResponseDto>> response = draftAccountController.postDraftAccountsSearch(
            searchDto, BEARER_TOKEN);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(draftAccountService, times(1)).searchDraftAccounts(any());
    }

    @Test
    void testSaveDraftAccounts_Success() {
        // Arrange
        DraftAccountEntity entity = DraftAccountEntity.builder()
            .accountType("Large")
            .accountStatus(DraftAccountStatus.SUBMITTED)
            .account(getAccountJson())
            .businessUnit(BusinessUnit.Lite.builder().build())
            .submittedBy("USER_ID")
            .timelineData(getTimelineJson())
            .build();
        AddDraftAccountRequestDto addDraftAccountDto = AddDraftAccountRequestDto.builder()
            .accountType("Large")
            .account(getAccountJson())
            .businessUnitId((short)1)
            .submittedBy("USER_ID")
            .submittedByName("USER_NAME")
            .timelineData(getTimelineJson())
            .build();

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(new UserState.DeveloperUserState());
        when(draftAccountService.submitDraftAccount(any())).thenReturn(entity);

        // Act
        ResponseEntity<DraftAccountResponseDto> response = draftAccountController.postDraftAccount(
            addDraftAccountDto, BEARER_TOKEN);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        DraftAccountResponseDto responseEntity = response.getBody();
        assertEquals("Large", responseEntity.getAccountType());
        assertEquals("Submitted", responseEntity.getAccountStatus().getLabel());
        assertEquals(getAccountJson(), responseEntity.getAccount());
        assertEquals("USER_ID", responseEntity.getSubmittedBy());
        assertEquals(getTimelineJson(), responseEntity.getTimelineData());
        verify(draftAccountService, times(1)).submitDraftAccount(any());
    }

    @Test
    void testDeleteDraftAccount_Success() {
        // Arrange
        when(draftAccountService.deleteDraftAccount(any(Long.class), any(Boolean.class), any())).thenReturn(true);

        // Act
        ResponseEntity<String> response = draftAccountController
            .deleteDraftAccountById(7L, BEARER_TOKEN, Optional.empty());

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("""
                         { "message": "Draft Account '7' deleted"}""", response.getBody());
        verify(draftAccountService, times(1)).deleteDraftAccount(any(Long.class),
                                                                 any(Boolean.class), any());
    }

    @Test
    void testDeleteDraftAccount_Fail() {
        // Arrange
        when(draftAccountService.deleteDraftAccount(any(Long.class), any(Boolean.class), any())).thenThrow(
            new UnexpectedRollbackException("Entity 7L not found.")
        );

        // Act
        RuntimeException rte = assertThrows(UnexpectedRollbackException.class, () ->
            draftAccountController.deleteDraftAccountById(7L, BEARER_TOKEN, Optional.empty())
        );

        // Assert
        assertEquals("Entity 7L not found.", rte.getMessage());
        verify(draftAccountService, times(1)).deleteDraftAccount(any(Long.class),
                                                                 any(Boolean.class), any());
    }

    DraftAccountResponseDto toGetDto(DraftAccountEntity entity) {
        return DraftAccountResponseDto.builder()
            .draftAccountId(entity.getDraftAccountId())
            .businessUnitId(entity.getBusinessUnit().getBusinessUnitId())
            .createdDate(toUtcDateTime(entity.getCreatedDate()))
            .submittedBy(entity.getSubmittedBy())
            .validatedDate(toUtcDateTime(entity.getValidatedDate()))
            .validatedBy(entity.getValidatedBy())
            .validatedByName(entity.getValidatedByName())
            .account(entity.getAccount())
            .accountSnapshot(entity.getAccountSnapshot())
            .accountType(entity.getAccountType())
            .accountStatus(entity.getAccountStatus())
            .timelineData(entity.getTimelineData())
            .accountNumber(entity.getAccountNumber())
            .accountId(entity.getAccountId())
            .build();
    }

    DraftAccountSummaryDto toSummaryDto(DraftAccountEntity entity) {
        return DraftAccountSummaryDto.builder()
            .draftAccountId(entity.getDraftAccountId())
            .businessUnitId(entity.getBusinessUnit().getBusinessUnitId())
            .createdDate(toUtcDateTime(entity.getCreatedDate()))
            .submittedBy(entity.getSubmittedBy())
            .validatedDate(toUtcDateTime(entity.getValidatedDate()))
            .validatedBy(entity.getValidatedBy())
            .validatedByName(entity.getValidatedByName())
            .accountSnapshot(entity.getAccountSnapshot())
            .accountType(entity.getAccountType())
            .accountStatus(entity.getAccountStatus())
            .accountNumber(entity.getAccountNumber())
            .accountId(entity.getAccountId())
            .build();
    }

    private String getAccountJson() {
        return """
               {
                 "account_type": "fine",
                 "defendant_type": "company",
                 "originator_name": "Asylum & Immigration Tribunal",
                 "originator_id": 3865,
                 "prosecutor_case_reference": "AB123456",
                 "enforcement_court_id": 6255,
                 "collection_order_made": true,
                 "collection_order_made_today": true,
                 "collection_order_date": null,
                 "suspended_committal_date": null,
                 "payment_card_request": true,
                 "account_sentence_date": "2025-01-01",
                 "defendant": {
                     "company_flag": true,
                     "title": null,
                     "surname": null,
                     "forenames": null,
                     "company_name": "Acme Co Ltd",
                     "dob": null,
                     "address_line_1": "1 Test Lane",
                     "address_line_2": null,
                     "address_line_3": null,
                     "address_line_4": null,
                     "address_line_5": null,
                     "post_code": null,
                     "telephone_number_home": null,
                     "telephone_number_business": null,
                     "telephone_number_mobile": null,
                     "email_address_1": null,
                     "email_address_2": null,
                     "national_insurance_number": null,
                     "driving_licence_number": null,
                     "pnc_id": null,
                     "nationality_1": null,
                     "nationality_2": null,
                     "ethnicity_self_defined": null,
                     "ethnicity_observed": null,
                     "cro_number": null,
                     "occupation": null,
                     "gender": null,
                     "custody_status": null,
                     "prison_number": null,
                     "interpreter_lang": null,
                     "debtor_detail": {
                         "vehicle_make": null,
                         "vehicle_registration_mark": null,
                         "document_language": "EN",
                         "hearing_language": "EN",
                         "employee_reference": null,
                         "employer_company_name": null,
                         "employer_address_line_1": null,
                         "employer_address_line_2": null,
                         "employer_address_line_3": null,
                         "employer_address_line_4": null,
                         "employer_address_line_5": null,
                         "employer_post_code": null,
                         "employer_telephone_number": null,
                         "employer_email_address": null,
                         "aliases": null
                     },
                     "parent_guardian": null
                 },
                 "offences": [
                     {
                         "date_of_sentence": "01/01/2025",
                         "imposing_court_id": 6255,
                         "offence_id": 35014,
                         "impositions": [
                             {
                                 "result_id": "100",
                                 "amount_imposed": 100,
                                 "amount_paid": 0,
                                 "major_creditor_id": null,
                                 "minor_creditor": null
                             }
                         ]
                     }
                 ],
                 "fp_ticket_detail": null,
                 "payment_terms": {
                     "payment_terms_type_code": "B",
                     "effective_date": "2025-01-31",
                     "instalment_period": null,
                     "lump_sum_amount": null,
                     "instalment_amount": null,
                     "default_days_in_jail": null,
                     "enforcements": null
                 },
                 "account_notes": null
            }""";
    }

    private String getTimelineJson() {
        return """
               [
                    {
                        "username": "johndoe123",
                        "status": "Active",
                        "status_date": "2023-11-01",
                        "reason_text": "Account successfully activated after review."
                    },
                    {
                        "username": "janedoe456",
                        "status": "Pending",
                        "status_date": "2023-12-05",
                        "reason_text": "Awaiting additional documentation for verification."
                    },
                    {
                        "username": "mikebrown789",
                        "status": "Suspended",
                        "status_date": "2023-10-15",
                        "reason_text": "Violation of terms of service."
                    }
                ]""";
    }
}
