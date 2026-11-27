package uk.gov.hmcts.opal.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.generated.model.BusinessUnitSummaryLiteCommon;
import uk.gov.hmcts.opal.generated.model.SuspenseItemSummaryCommonStrict;
import uk.gov.hmcts.opal.service.SuspenseAccountsSummaryService;

@ExtendWith(MockitoExtension.class)
public class SuspenseAccountsApiControllerTest {

    @Mock
    private SuspenseAccountsSummaryService suspenseAccountsSummaryService;

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

}
