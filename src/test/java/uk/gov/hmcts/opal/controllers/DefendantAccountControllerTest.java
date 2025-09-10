package uk.gov.hmcts.opal.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
import uk.gov.hmcts.opal.dto.response.GetHeaderSummaryResponse;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.DefendantAccountSearchResultsDto;
import uk.gov.hmcts.opal.service.DefendantAccountService;
import uk.gov.hmcts.opal.service.legacy.LegacyDefendantAccountService;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.gov.hmcts.opal.service.proxy.DefendantAccountServiceProxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class DefendantAccountControllerTest {

    static final String BEARER_TOKEN = "Bearer a_token_here";

    @Mock
    private DefendantAccountService defendantAccountService;

    @Mock
    private UserStateService userStateService;

    @Mock
    private DefendantAccountServiceProxy defendantAccountServiceProxy;

    @InjectMocks
    private DefendantAccountController defendantAccountController;

    @Test
    void testPostDefendantAccountSearch_Success() {
        // Arrange
        AccountSearchDto requestEntity = AccountSearchDto.builder().build();
        DefendantAccountSearchResultsDto mockResponse = DefendantAccountSearchResultsDto.builder().build();

        when(defendantAccountService.searchDefendantAccounts(any(AccountSearchDto.class), eq(BEARER_TOKEN)))
            .thenReturn(mockResponse);

        // Act
        ResponseEntity<DefendantAccountSearchResultsDto> responseEntity =
            defendantAccountController.postDefendantAccountSearch(requestEntity, BEARER_TOKEN);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(mockResponse, responseEntity.getBody());

        verify(defendantAccountService, times(1))
            .searchDefendantAccounts(any(AccountSearchDto.class), eq(BEARER_TOKEN));
    }

    @Test
    void testGetHeaderSummary_Success() {
        // Arrange
        DefendantAccountHeaderSummary mockBody = new DefendantAccountHeaderSummary();

        var userWithPermission = uk.gov.hmcts.opal.authorisation.model.UserState.builder()
            .userId(99L)
            .userName("tester")
            .businessUnitUser(java.util.Set.of(
                uk.gov.hmcts.opal.authorisation.model.BusinessUnitUser.builder()
                    .businessUnitUserId("1L")
                    .businessUnitId((short) 78)
                    .build()
            ))
            .build();

        when(defendantAccountService.getHeaderSummary(eq(1L), any()))
            .thenReturn(mockBody);

        // Act
        ResponseEntity<DefendantAccountHeaderSummary> response =
            defendantAccountController.getHeaderSummary(1L, BEARER_TOKEN);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockBody, response.getBody());

        verify(defendantAccountService).getHeaderSummary(eq(1L), any());
    }

    @Test
    void testGetHeaderSummaryResponseGetters() {
        DefendantAccountHeaderSummary summary = DefendantAccountHeaderSummary.builder()
            .accountNumber("ABCD")
            .accountType("Fine")
            .build();
        Long version = 10L;

        GetHeaderSummaryResponse resp = new GetHeaderSummaryResponse(summary, version);
        assertEquals(summary, resp.getData());
        assertEquals(version, resp.getVersion());

        // Null cases
        GetHeaderSummaryResponse respNull = new GetHeaderSummaryResponse(null, null);
        assertNull(respNull.getData());
        assertNull(respNull.getVersion());
    }

    @Test
    void testLegacyDefendantAccountService_createGetDefendantAccountRequest() {
        String id = "77";
        var req = LegacyDefendantAccountService.createGetDefendantAccountRequest(id);
        assertNotNull(req);
        assertEquals(id, req.getDefendantAccountId());
    }

}
