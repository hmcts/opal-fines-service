package uk.gov.hmcts.opal.controllers;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.generated.model.AddressDetailsCommon;
import uk.gov.hmcts.opal.generated.model.AdjustmentDetailsCommonStrict;
import uk.gov.hmcts.opal.generated.model.BusinessUnitSummaryLiteCommon;
import uk.gov.hmcts.opal.generated.model.NoteCommon;
import uk.gov.hmcts.opal.generated.model.NoteCommon.NoteTypeEnum;
import uk.gov.hmcts.opal.generated.model.NoteCommon.RecordTypeEnum;
import uk.gov.hmcts.opal.generated.model.SuspenseItemHistoryCommonStrict;
import uk.gov.hmcts.opal.generated.model.SuspenseItemSummaryCommonStrict;
import uk.gov.hmcts.opal.generated.model.SuspenseNoteCommonStrict;
import uk.gov.hmcts.opal.generated.model.SuspenseTransactionCommonStrict;
import uk.gov.hmcts.opal.generated.model.TransactionTypeCommonStrict;
import uk.gov.hmcts.opal.service.SuspenseAccountsHistoryService;
import uk.gov.hmcts.opal.service.SuspenseAccountsSummaryService;

@ExtendWith(MockitoExtension.class)
public class SuspenseAccountsApiControllerTest {

    @Mock
    private SuspenseAccountsSummaryService suspenseAccountsSummaryService;

    @Mock
    private SuspenseAccountsHistoryService suspenseAccountsHistoryService;

    @InjectMocks
    private SuspenseAccountsApiController suspenseAccountsApiController;

    @Test
    public void testGetSuspenseAccountsSummary() throws Exception {
        //  Set up test data
        SuspenseItemSummaryCommonStrict itemSummary = SuspenseItemSummaryCommonStrict
            .builder()
            .businessUnitDetails(BusinessUnitSummaryLiteCommon.builder()
                .businessUnitId((short) 123)
                .businessUnitName("Test Business Unit")
                .build())
            .suspenseItemId(123)
            .suspenseItemNumber(123)
            .createdDate(LocalDateTime.of(2026, 11, 3, 0, 0))
            .suspenseItemType("Test Suspense Item Type")
            .balance("100.0")
            .build();

        //  Mock the service
        when(suspenseAccountsSummaryService.getSuspenseAccountsSummary(any(), any())).thenReturn(itemSummary);

        //  Call the method under test
        ResponseEntity<SuspenseItemSummaryCommonStrict> response =
            suspenseAccountsApiController.getSuspenseItemSummary(122, 123L);

        //  Assert the result
        assertEquals(itemSummary, response.getBody());

        assert response.getBody() != null;
        assert response.getBody().getSuspenseItemId() != null;
        assert response.getBody().getBusinessUnitDetails() != null;
        assert itemSummary.getBusinessUnitDetails() != null;
        assert itemSummary.getBusinessUnitDetails().getBusinessUnitId() != null;

        assert response.getBody().getBusinessUnitDetails().getBusinessUnitName() != null;
        assert itemSummary.getBusinessUnitDetails().getBusinessUnitName() != null;

        assertEquals(itemSummary.getBusinessUnitDetails().getBusinessUnitId(),
            response.getBody().getBusinessUnitDetails().getBusinessUnitId());
        assertEquals(itemSummary.getBusinessUnitDetails().getBusinessUnitName(),
            response.getBody().getBusinessUnitDetails().getBusinessUnitName());
        assertEquals(itemSummary.getSuspenseItemId(), response.getBody().getSuspenseItemId());
        assertEquals(itemSummary.getSuspenseItemNumber(), response.getBody().getSuspenseItemNumber());
        assertEquals(itemSummary.getCreatedDate(), response.getBody().getCreatedDate());
        assertEquals(itemSummary.getSuspenseItemType(), response.getBody().getSuspenseItemType());
        assertEquals(itemSummary.getBalance(), response.getBody().getBalance());
    }

    @Test
    public void testGetSuspenseAccountItemHistoryAndTransactions() throws Exception {
        SuspenseItemHistoryCommonStrict itemHistory = SuspenseItemHistoryCommonStrict
            .builder()
            .businessUnitId(1)
            .suspenseItemId(1)
            .suspenseNotes(Collections.singletonList(
                SuspenseNoteCommonStrict
                    .builder()
                    .note(NoteCommon
                        .builder()
                        .recordType(RecordTypeEnum.SUSPENSE_ITEMS)
                        .noteText("Test Note")
                        .noteType(NoteTypeEnum.AA)
                        .build())
                    .build()))
            .suspenseTransactions(List.of(
                SuspenseTransactionCommonStrict
                    .builder()
                    .transactionId(1L)
                    .transactionType(TransactionTypeCommonStrict.builder().build())
                    .amount(100)
                    .details(AdjustmentDetailsCommonStrict
                        .builder()
                        .creditorAddress(AddressDetailsCommon
                            .builder()
                            .addressLine1("123 Test Street")
                            .addressLine2("Test Area")
                            .postcode("AB12 3CD")
                            .build())
                        .build())
                    .build()
            ))
            .build();

        when(suspenseAccountsHistoryService.getSuspenseItemHistory(any(), any())).thenReturn(itemHistory);

        //  Call the API Controller:
        ResponseEntity<SuspenseItemHistoryCommonStrict> response =
            suspenseAccountsApiController.getSuspenseItemHistory(1L, 1L);

        assertEquals(itemHistory, response.getBody());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getBusinessUnitId());
        assertNotNull(response.getBody().getSuspenseItemId());
        assertNotNull(response.getBody().getSuspenseNotes());
        assertNotNull(response.getBody().getSuspenseNotes().get().getFirst().getNote());
        assertNotNull(
            Objects.requireNonNull(response.getBody().getSuspenseNotes().get().getFirst().getNote()).getNoteText());

        assertEquals(1, response.getBody().getBusinessUnitId());
        assertEquals(1, response.getBody().getSuspenseItemId());
        assertEquals(1, response.getBody().getSuspenseNotes().get().size());
        assertEquals(RecordTypeEnum.SUSPENSE_ITEMS, Objects.requireNonNull(
            response.getBody().getSuspenseNotes().get().getFirst().getNote()).getRecordType());
        assertEquals("Test Note", Objects.requireNonNull(
            response.getBody().getSuspenseNotes().get().getFirst().getNote()).getNoteText());
        assertEquals(NoteTypeEnum.AA, Objects.requireNonNull(
            response.getBody().getSuspenseNotes().get().getFirst().getNote()).getNoteType());
        assertEquals(1, response.getBody().getSuspenseTransactions().get().size());
    }

}
