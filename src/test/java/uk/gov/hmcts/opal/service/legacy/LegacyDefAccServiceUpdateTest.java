package uk.gov.hmcts.opal.service.legacy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.opal.service.legacy.LegacyDefendantAccountService.PATCH_DEFENDANT_ACCOUNT;

import java.math.BigInteger;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.hmcts.opal.common.legacy.model.ErrorResponse;
import uk.gov.hmcts.opal.common.legacy.service.GatewayService;
import uk.gov.hmcts.opal.common.legacy.service.GatewayService.Response;
import uk.gov.hmcts.opal.dto.UpdateDefendantAccountResponse;
import uk.gov.hmcts.opal.dto.UpdateDefendantAccountRequest;
import uk.gov.hmcts.opal.dto.legacy.LegacyUpdateDefendantAccountRequest;
import uk.gov.hmcts.opal.dto.legacy.LegacyUpdateDefendantAccountResponse;
import uk.gov.hmcts.opal.generated.model.CommentsAndNotesCommon;
import uk.gov.hmcts.opal.generated.model.UpdateDefendantAccountRequestPayload;

class LegacyDefAccServiceUpdateTest extends AbstractLegacyDefAccServiceTest {

    @Test
    void testUpdateDefendantAccount_happyPath_buildsLegacyRequest_callsGateway_andMapsResponse() {
        final String postedBy = "user-123";
        long defendantAccountId = 77L;
        String businessUnitId = "78";

        UpdateDefendantAccountRequest request = UpdateDefendantAccountRequest.builder()
            .defendantAccountId(defendantAccountId)
            .businessUnitId(businessUnitId)
            .businessUnitUserId(postedBy)
            .payload(UpdateDefendantAccountRequestPayload.builder()
                .commentAndNotes(CommentsAndNotesCommon.builder()
                    .accountComment("x")
                    .build())
                .build())
            .version(BigInteger.valueOf(3))
            .build();

        LegacyUpdateDefendantAccountRequest legacyReq = LegacyUpdateDefendantAccountRequest.builder()
            .defendantAccountId(String.valueOf(defendantAccountId))
            .businessUnitId(businessUnitId)
            .businessUnitUserId(postedBy)
            .version(BigInteger.valueOf(3))
            .build();

        when(updateDefendantAccountRequestMapper.toLegacyUpdateDefendantAccountRequest(request))
            .thenReturn(legacyReq);

        LegacyUpdateDefendantAccountResponse legacyEntity = new LegacyUpdateDefendantAccountResponse();
        GatewayService.Response<LegacyUpdateDefendantAccountResponse> resp =
            new GatewayService.Response<>(HttpStatus.OK, legacyEntity, null, null);

        doReturn(resp).when(gatewayService).postToGateway(
            eq(PATCH_DEFENDANT_ACCOUNT),
            eq(LegacyUpdateDefendantAccountResponse.class),
            any(LegacyUpdateDefendantAccountRequest.class),
            nullable(String.class)
        );

        UpdateDefendantAccountResponse expected = UpdateDefendantAccountResponse.builder()
            .version(BigInteger.valueOf(3))
            .build();
        when(legacyUpdateDefendantAccountResponseMapper.toUpdateDefendantAccountResponse(legacyEntity))
            .thenReturn(expected);

        UpdateDefendantAccountResponse result = legacyDefendantAccountService
            .updateDefendantAccount(defendantAccountId, businessUnitId, request, postedBy, "Poster Name");

        assertThat(result).isSameAs(expected);

        verify(updateDefendantAccountRequestMapper).toLegacyUpdateDefendantAccountRequest(request);
        verify(gatewayService).postToGateway(
            eq(PATCH_DEFENDANT_ACCOUNT),
            eq(LegacyUpdateDefendantAccountResponse.class),
            eq(legacyReq),
            isNull()
        );
        verify(legacyUpdateDefendantAccountResponseMapper).toUpdateDefendantAccountResponse(legacyEntity);
    }

    @Test
    void testUpdateDefendantAccount_whenGatewayThrows_exceptionPropagates() {
        final String postedBy = "user-123";
        long defendantAccountId = 77L;
        String businessUnitId = "78";

        UpdateDefendantAccountRequest request = UpdateDefendantAccountRequest.builder()
            .defendantAccountId(defendantAccountId)
            .businessUnitId(businessUnitId)
            .businessUnitUserId(postedBy)
            .payload(UpdateDefendantAccountRequestPayload.builder()
                .commentAndNotes(CommentsAndNotesCommon.builder()
                    .accountComment("x")
                    .build())
                .build())
            .version(BigInteger.valueOf(5))
            .build();

        when(updateDefendantAccountRequestMapper.toLegacyUpdateDefendantAccountRequest(request))
            .thenReturn(LegacyUpdateDefendantAccountRequest.builder().build());

        when(gatewayService.postToGateway(
            eq(PATCH_DEFENDANT_ACCOUNT),
            eq(LegacyUpdateDefendantAccountResponse.class),
            any(),
            isNull()))
            .thenThrow(new RuntimeException("Simulate Run Time Exception from gateway"));

        assertThrows(RuntimeException.class, () ->
            legacyDefendantAccountService.updateDefendantAccount(
                defendantAccountId, businessUnitId, request, postedBy, "Poster Name")
        );

        verify(gatewayService).postToGateway(
            eq(PATCH_DEFENDANT_ACCOUNT),
            eq(LegacyUpdateDefendantAccountResponse.class),
            any(LegacyUpdateDefendantAccountRequest.class),
            isNull()
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void testUpdateDefendantAccount_error5xx_noEntity_throwsHttpServerErrorException() {
        ParameterizedTypeReference<LegacyUpdateDefendantAccountResponse> typeRef =
            new ParameterizedTypeReference<>() {};
        when(restClient.responseSpec.body(any(typeRef.getClass()))).thenReturn(null);
        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>("<response><error/></response>", HttpStatus.INTERNAL_SERVER_ERROR));

        UpdateDefendantAccountRequest request = UpdateDefendantAccountRequest.builder()
            .defendantAccountId(77L)
            .businessUnitId("78")
            .businessUnitUserId("postedBy")
            .payload(UpdateDefendantAccountRequestPayload.builder()
                .commentAndNotes(CommentsAndNotesCommon.builder()
                    .accountComment("x")
                    .build())
                .build())
            .version(BigInteger.ONE)
            .build();

        assertThatThrownBy(() -> legacyDefendantAccountService.updateDefendantAccount(77L, "78", request, "postedBy",
            "Poster Name")).isInstanceOf(HttpServerErrorException.class)
            .hasMessage("500 Legacy gateway returned failure");
    }

    @Test
    void testUpdateDefendantAccount_embeddedLegacyError_throwsUnprocessableException() {
        final String postedBy = "user-123";
        final long defendantAccountId = 77L;
        final String businessUnitId = "78";

        UpdateDefendantAccountRequest request =
            UpdateDefendantAccountRequest.builder().defendantAccountId(defendantAccountId)
                .businessUnitId(businessUnitId).businessUnitUserId(postedBy).payload(
                    UpdateDefendantAccountRequestPayload.builder()
                        .commentAndNotes(CommentsAndNotesCommon.builder().accountComment("x").build()).build())
                .version(BigInteger.valueOf(5)).build();

        LegacyUpdateDefendantAccountResponse legacyEntity = LegacyUpdateDefendantAccountResponse.builder()
            .errorResponse(new ErrorResponse("-20020", "Enforcer 50000000003 not found")).build();

        Response<LegacyUpdateDefendantAccountResponse> response =
            new Response<>(HttpStatus.OK, legacyEntity, null, null);

        when(updateDefendantAccountRequestMapper.toLegacyUpdateDefendantAccountRequest(request)).thenReturn(
            LegacyUpdateDefendantAccountRequest.builder().build());

        when(gatewayService.postToGateway(eq(LegacyDefendantAccountService.PATCH_DEFENDANT_ACCOUNT),
            eq(LegacyUpdateDefendantAccountResponse.class), any(LegacyUpdateDefendantAccountRequest.class),
            nullable(String.class))).thenReturn(response);

        assertThatThrownBy(
            () -> legacyDefendantAccountService.updateDefendantAccount(defendantAccountId, businessUnitId, request,
                postedBy, "Poster Name")).isInstanceOf(HttpServerErrorException.class)
            .hasMessage("500 Legacy gateway error");

        verify(legacyUpdateDefendantAccountResponseMapper, never()).toUpdateDefendantAccountResponse(
            any(LegacyUpdateDefendantAccountResponse.class));
    }
}
