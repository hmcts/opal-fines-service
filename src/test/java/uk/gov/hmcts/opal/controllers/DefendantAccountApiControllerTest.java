package uk.gov.hmcts.opal.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.EnforcementStatus;
import uk.gov.hmcts.opal.generated.model.GetEnforcementStatusResponse;
import uk.gov.hmcts.opal.generated.model.PostDefendantAccountSearchRequestDefendantAccount;
import uk.gov.hmcts.opal.generated.model.PostDefendantAccountSearchResponseDefendantAccount;
import uk.gov.hmcts.opal.service.DefendantAccountService;


@ExtendWith(MockitoExtension.class)
class DefendantAccountApiControllerTest {

    private static final String BEARER_TOKEN = "Bearer a_token_goes_here";

    @Mock
    private DefendantAccountService defendantAccountService;

    @InjectMocks
    private DefendantAccountApiController defendantAccountApiController;

    @Test
    void given_validRequest_when_getEnforcementStatus_then_returnsOkResponse() {
        Long defendantId = 1L;
        EnforcementStatus status = EnforcementStatus.builder()
            .build();
        when(defendantAccountService.getEnforcementStatus(defendantId, BEARER_TOKEN))
            .thenReturn(status);

        ResponseEntity<GetEnforcementStatusResponse> response =
            defendantAccountApiController.getEnforcementStatus(defendantId, BEARER_TOKEN);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(status, response.getBody());
        verify(defendantAccountService).getEnforcementStatus(defendantId, BEARER_TOKEN);
    }

    @Test
    void postDefendantAccountSearch_returnsServiceResponse() {
        PostDefendantAccountSearchRequestDefendantAccount request =
            PostDefendantAccountSearchRequestDefendantAccount.builder()
                .activeAccountsOnly(true)
                .businessUnitIds(List.of(77))
                .build();
        PostDefendantAccountSearchResponseDefendantAccount serviceResponse =
            PostDefendantAccountSearchResponseDefendantAccount.builder()
                .count(0)
                .defendantAccounts(Collections.emptyList())
                .build();

        when(defendantAccountService.searchDefendantAccounts(request, BEARER_TOKEN))
            .thenReturn(serviceResponse);

        ResponseEntity<PostDefendantAccountSearchResponseDefendantAccount> response =
            defendantAccountApiController.postDefendantAccountSearch(request, BEARER_TOKEN);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(serviceResponse, response.getBody());
        verify(defendantAccountService).searchDefendantAccounts(request, BEARER_TOKEN);
    }

}
