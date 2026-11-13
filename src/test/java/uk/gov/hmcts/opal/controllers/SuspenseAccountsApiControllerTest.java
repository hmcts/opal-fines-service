package uk.gov.hmcts.opal.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.time.Month;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.generated.model.BusinessUnitSummaryLiteCommon;
import uk.gov.hmcts.opal.generated.model.SuspenseItemHistoryCommonStrict;
import uk.gov.hmcts.opal.generated.model.SuspenseItemSummaryCommonStrict;
import uk.gov.hmcts.opal.service.SuspenseAccountsHistoryService;
import uk.gov.hmcts.opal.service.SuspenseAccountsSummaryService;

@ExtendWith(MockitoExtension.class)
class SuspenseAccountsApiControllerTest {

    @Mock
    private SuspenseAccountsSummaryService suspenseAccountsSummaryService;

    @Mock
    private SuspenseAccountsHistoryService suspenseAccountsHistoryService;

    @InjectMocks
    private SuspenseAccountsApiController suspenseAccountsApiController;

    @Test
    void testGetSuspenseAccountsSummary() {
        //  Set up test data
        SuspenseItemSummaryCommonStrict itemSummary = SuspenseItemSummaryCommonStrict
            .builder()
            .businessUnitDetails(BusinessUnitSummaryLiteCommon.builder()
                .businessUnitId((short) 123)
                .businessUnitName("Test Business Unit")
                .build())
            .suspenseItemId(123L)
            .suspenseItemNumber(123L)
            .createdDate(LocalDateTime.of(2026, Month.NOVEMBER, 3, 0, 0))
            .suspenseItemType("Test Suspense Item Type")
            .balance("100.0")
            .build();

        //  Mock the service
        when(suspenseAccountsSummaryService.getSuspenseAccountsSummary(any(), any())).thenReturn(itemSummary);

        //  Call the method under test
        ResponseEntity<SuspenseItemSummaryCommonStrict> response =
            suspenseAccountsApiController.getSuspenseItemSummary((short) 122, 123L);

        //  Assert the result
        assertEquals(itemSummary, response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(suspenseAccountsSummaryService).getSuspenseAccountsSummary((short) 122, 123L);

        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getSuspenseItemId());
        assertNotNull(response.getBody().getBusinessUnitDetails());
        assertNotNull(itemSummary.getBusinessUnitDetails());
        assertNotNull(itemSummary.getBusinessUnitDetails().getBusinessUnitId());

        assertNotNull(response.getBody().getBusinessUnitDetails().getBusinessUnitName());
        assertNotNull(itemSummary.getBusinessUnitDetails().getBusinessUnitName());

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
    void getSummary_rejectsMissingItem() {
        when(suspenseAccountsSummaryService.getSuspenseAccountsSummary((short) 123, 12345L)).thenReturn(null);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
            () -> suspenseAccountsApiController.getSuspenseItemSummary((short) 123, 12345L));

        assertEquals("Suspense item summary not found", exception.getMessage());
    }

    @Test
    void getHistory_returnsServiceResponse() {
        SuspenseItemHistoryCommonStrict history = new SuspenseItemHistoryCommonStrict()
            .businessUnitId(123)
            .suspenseItemId(1);
        when(suspenseAccountsHistoryService.getSuspenseItemHistory(123L, 12345L)).thenReturn(history);

        ResponseEntity<SuspenseItemHistoryCommonStrict> response =
            suspenseAccountsApiController.getSuspenseItemHistory(123L, 12345L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(history, response.getBody());
        verify(suspenseAccountsHistoryService).getSuspenseItemHistory(123L, 12345L);
    }

}
