package uk.gov.hmcts.opal.service.legacy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.DefendantAccountResponse;
import uk.gov.hmcts.opal.dto.UpdateDefendantAccountRequest;
import uk.gov.hmcts.opal.dto.legacy.LegacyUpdateDefendantAccountRequest;
import uk.gov.hmcts.opal.dto.legacy.LegacyUpdateDefendantAccountResponse;

class LegacyDefAccServiceUpdateTest extends AbstractLegacyDefAccServiceTest {

    @Test
    void testUpdateDefendantAccount_happyPath_buildsLegacyRequest_callsGateway_andMapsResponse() {
        final String postedBy = "user-123";
        long defendantAccountId = 77L;
        String businessUnitId = "78";

        LegacyUpdateDefendantAccountRequest legacyReq = LegacyUpdateDefendantAccountRequest.builder()
            .defendantAccountId(String.valueOf(defendantAccountId))
            .businessUnitId(businessUnitId)
            .businessUnitUserId(postedBy)
            .version(3)
            .build();

        when(updateDefendantAccountRequestMapper.toLegacyUpdateDefendantAccountRequest(
            any(), anyString(), anyString(), anyString(), anyInt()
        )).thenReturn(legacyReq);

        LegacyUpdateDefendantAccountResponse legacyEntity = new LegacyUpdateDefendantAccountResponse();
        GatewayService.Response<LegacyUpdateDefendantAccountResponse> resp =
            new GatewayService.Response<>(HttpStatus.OK, legacyEntity, null, null);

        doReturn(resp).when(gatewayService).postToGateway(
            eq(LegacyDefendantAccountService.PATCH_DEFENDANT_ACCOUNT),
            eq(LegacyUpdateDefendantAccountResponse.class),
            any(LegacyUpdateDefendantAccountRequest.class),
            Mockito.nullable(String.class)
        );

        DefendantAccountResponse expected = DefendantAccountResponse.builder().id(defendantAccountId).build();
        when(legacyUpdateDefendantAccountResponseMapper.toDefendantAccountResponse(legacyEntity)).thenReturn(expected);

        DefendantAccountResponse result = legacyDefendantAccountService
            .updateDefendantAccount(defendantAccountId, businessUnitId, updateDefendantAccountRequest, "3", postedBy);

        assertThat(result).isSameAs(expected);

        verify(updateDefendantAccountRequestMapper).toLegacyUpdateDefendantAccountRequest(
            same(updateDefendantAccountRequest), eq("77"), eq("78"), eq(postedBy), eq(3)
        );
        verify(gatewayService).postToGateway(
            eq(LegacyDefendantAccountService.PATCH_DEFENDANT_ACCOUNT),
            eq(LegacyUpdateDefendantAccountResponse.class),
            eq(legacyReq),
            isNull()
        );
        verify(legacyUpdateDefendantAccountResponseMapper).toDefendantAccountResponse(legacyEntity);
    }

    @Test
    void testUpdateDefendantAccount_whenGatewayThrows_exceptionPropagates() {
        final String postedBy = "user-123";
        long defendantAccountId = 77L;
        String businessUnitId = "78";

        when(updateDefendantAccountRequestMapper
            .toLegacyUpdateDefendantAccountRequest(any(), anyString(), anyString(), anyString(), anyInt()))
            .thenReturn(LegacyUpdateDefendantAccountRequest.builder().build());

        when(gatewayService.postToGateway(eq(LegacyDefendantAccountService.PATCH_DEFENDANT_ACCOUNT),
            eq(LegacyUpdateDefendantAccountResponse.class), any(), isNull()))
            .thenThrow(new RuntimeException("Simulate Run Time Exception from gateway"));

        assertThrows(RuntimeException.class, () ->
            legacyDefendantAccountService
                .updateDefendantAccount(defendantAccountId, businessUnitId, mock(UpdateDefendantAccountRequest.class),
                    "5", postedBy)
        );

        verify(gatewayService).postToGateway(
            eq("LIBRA.patchDefendantAccount"),
            eq(LegacyUpdateDefendantAccountResponse.class),
            any(LegacyUpdateDefendantAccountRequest.class),
            isNull()
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void testUpdateDefendantAccount_error5xx_noEntity_returnsNull() {
        ParameterizedTypeReference<LegacyUpdateDefendantAccountResponse> typeRef =
            new ParameterizedTypeReference<>() {};
        when(restClient.responseSpec.body(any(typeRef.getClass()))).thenReturn(null);
        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>("<response><error/></response>", HttpStatus.INTERNAL_SERVER_ERROR));

        DefendantAccountResponse response = legacyDefendantAccountService
            .updateDefendantAccount(77L, "78", mock(UpdateDefendantAccountRequest.class), "1", "postedBy");
        assertNull(response);
    }
}
