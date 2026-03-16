package uk.gov.hmcts.opal.service.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyShort;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.EnforcementStatus;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountEnforcementStatusResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountEnforcementStatusResponse.EnforcementAction;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountEnforcementStatusResponse.EnforcementOverview;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.dto.legacy.common.CollectionOrder;
import uk.gov.hmcts.opal.dto.legacy.common.CourtReference;
import uk.gov.hmcts.opal.dto.legacy.common.EnforcementOverride;
import uk.gov.hmcts.opal.dto.legacy.common.EnforcementOverrideResultReference;
import uk.gov.hmcts.opal.dto.legacy.common.EnforcerReference;
import uk.gov.hmcts.opal.dto.legacy.common.LjaReference;
import uk.gov.hmcts.opal.dto.legacy.common.ResultReference;
import uk.gov.hmcts.opal.dto.legacy.common.ResultResponses;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity;
import uk.gov.hmcts.opal.entity.court.CourtEntity.Lite;
import uk.gov.hmcts.opal.generated.model.AccountStatusReferenceCommon;
import uk.gov.hmcts.opal.generated.model.AccountStatusReferenceCommon.AccountStatusCodeEnum;
import uk.gov.hmcts.opal.generated.model.EnforcementActionDefendantAccount;
import uk.gov.hmcts.opal.generated.model.EnforcementOverrideCommon;
import uk.gov.hmcts.opal.generated.model.EnforcementOverviewDefendantAccount;

class LegacyDefAccServiceEnforcementStatusTest extends AbstractLegacyDefAccServiceTest {

    @Test
    @SuppressWarnings("unchecked")
    void testGetEnforcementStatus_success() {
        // Arrange
        LegacyGetDefendantAccountEnforcementStatusResponse responseBody =
            createLegacyEnforcementStatusResponse(true);

        when(restClient.responseSpec
            .body(Mockito.<ParameterizedTypeReference<LegacyGetDefendantAccountEnforcementStatusResponse>>any()))
            .thenReturn(responseBody);

        when(courtService.getCourtById(anyLong())).thenReturn(Lite.builder().courtCode((short)123).build());
        when(ljaService.getLocalJusticeAreaById(anyShort())).thenReturn(
            LocalJusticeAreaEntity.builder().ljaCode("6-7").build());

        ResponseEntity<String> serverSuccessResponse =
            new ResponseEntity<>(responseBody.toXml(), HttpStatus.OK);
        when(restClient.responseSpec.toEntity(String.class)).thenReturn(serverSuccessResponse);

        // Act
        EnforcementStatus response = legacyDefendantAccountService
            .getEnforcementStatus(33L);

        // Assert
        assertNotNull(response);
        assertTrue(response.getEmployerFlag());
        assertEquals(new BigInteger("1234567890123456789012345678901234567890"), response.getVersion());
        assertFalse(response.getIsHmrcCheckEligible());
        assertNull(response.getNextEnforcementActionData());
        assertNotNull(response.getEnforcementOverride());
        assertNotNull(response.getLastEnforcementAction());
        assertNotNull(response.getEnforcementOverview());
        assertNotNull(response.getAccountStatusReference());

        EnforcementOverrideCommon override = response.getEnforcementOverride();
        assertNotNull(override.getEnforcementOverrideResult());
        assertEquals("AAB", override.getEnforcementOverrideResult().getEnforcementOverrideResultId());
        assertEquals("AaAaBb", override.getEnforcementOverrideResult().getEnforcementOverrideResultName());
        assertNotNull(override.getEnforcer());
        assertEquals(2L, override.getEnforcer().getEnforcerId());
        assertEquals("Arthur", override.getEnforcer().getEnforcerName());
        assertNotNull(override.getLja());
        assertEquals(1, override.getLja().getLjaId());
        assertEquals("6-7", override.getLja().getLjaCode());
        assertEquals("England", override.getLja().getLjaName());

        EnforcementActionDefendantAccount action = response.getLastEnforcementAction();
        assertEquals("late", action.getReason());
        assertEquals("123", action.getWarrantNumber());
        assertEquals(LocalDateTime.of(2024, 1, 1, 10, 0), action.getDateAdded());
        assertNotNull(action.getEnforcer());
        assertEquals(4L, action.getEnforcer().getEnforcerId());
        assertEquals("Merlin", action.getEnforcer().getEnforcerName());
        assertNotNull(action.getEnforcementAction());
        assertEquals("FEE", action.getEnforcementAction().getResultId());
        assertEquals("Result Ref", action.getEnforcementAction().getResultTitle());
        assertNotNull(action.getResultResponses());
        assertNotNull(action.getResultResponses().getFirst());
        assertEquals("Param Name", action.getResultResponses().getFirst().getParameterName());
        assertEquals("A response", action.getResultResponses().getFirst().getResponse());

        EnforcementOverviewDefendantAccount overview = response.getEnforcementOverview();
        assertEquals(6, overview.getDaysInDefault());
        assertNotNull(overview.getCollectionOrder());
        assertEquals(true, overview.getCollectionOrder().getCollectionOrderFlag());
        assertEquals(LocalDate.of(2024, 3, 4), overview.getCollectionOrder().getCollectionOrderDate());
        assertNotNull(overview.getEnforcementCourt());
        assertEquals(3, overview.getEnforcementCourt().getCourtId());
        assertEquals(123, overview.getEnforcementCourt().getCourtCode());
        assertEquals("Bath", overview.getEnforcementCourt().getCourtName());

        AccountStatusReferenceCommon statusRef = response.getAccountStatusReference();
        assertEquals(AccountStatusCodeEnum.L, statusRef.getAccountStatusCode());
        assertEquals("Alive", statusRef.getAccountStatusDisplayName());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testGetEnforcementStatus_successMinimal() {
        // Arrange
        LegacyGetDefendantAccountEnforcementStatusResponse responseBody =
            createLegacyEnforcementStatusResponse(false);

        when(restClient.responseSpec.body(
            Mockito.<ParameterizedTypeReference<LegacyGetDefendantAccountEnforcementStatusResponse>>any()
        )).thenReturn(responseBody);

        ResponseEntity<String> serverSuccessResponse = new ResponseEntity<>(responseBody.toXml(), HttpStatus.OK);
        when(restClient.responseSpec.toEntity(String.class)).thenReturn(serverSuccessResponse);
        when(courtService.getCourtById(anyLong())).thenReturn(Lite.builder().courtCode((short)123).build());

        // Act
        EnforcementStatus response = legacyDefendantAccountService.getEnforcementStatus(72L);

        // Assert
        assertNotNull(response);
        assertTrue(response.getEmployerFlag());
        assertEquals(new BigInteger("1234567890123456789012345678901234567890"), response.getVersion());
        assertFalse(response.getIsHmrcCheckEligible());
        assertNull(response.getNextEnforcementActionData());
        assertNull(response.getEnforcementOverride());
        assertNull(response.getLastEnforcementAction());
        assertNotNull(response.getEnforcementOverview());
        assertNotNull(response.getAccountStatusReference());

        EnforcementOverviewDefendantAccount overview = response.getEnforcementOverview();
        assertEquals(6, overview.getDaysInDefault());
        assertNotNull(overview.getCollectionOrder());
        assertEquals(true, overview.getCollectionOrder().getCollectionOrderFlag());
        assertEquals(LocalDate.of(2024, 3, 4), overview.getCollectionOrder().getCollectionOrderDate());
        assertNotNull(overview.getEnforcementCourt());
        assertEquals(3, overview.getEnforcementCourt().getCourtId());
        assertEquals("Bath", overview.getEnforcementCourt().getCourtName());

        AccountStatusReferenceCommon statusRef = response.getAccountStatusReference();
        assertEquals(AccountStatusCodeEnum.L, statusRef.getAccountStatusCode());
        assertEquals("Alive", statusRef.getAccountStatusDisplayName());
    }

    @Test
    void testGetEnforcementStatus_throwsRuntimeException() {
        // Arrange
        doThrow(new RuntimeException("boom"))
            .when(gatewayService)
            .postToGateway(any(), any(), any(), any());

        // Act + Assert
        assertThrows(RuntimeException.class, () -> legacyDefendantAccountService.getEnforcementStatus(1L));
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetEnforcementStatus_returnsNull() {
        // Arrange
        when(restClient.responseSpec.body(
            Mockito.<ParameterizedTypeReference<LegacyGetDefendantAccountPaymentTermsResponse>>any())).thenReturn(null);
        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>("<error/>", HttpStatus.INTERNAL_SERVER_ERROR));

        // Act
        EnforcementStatus response = legacyDefendantAccountService.getEnforcementStatus(42L);

        // Assert
        assertNull(response);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetEnforcementStatus_returnsFailure() {

        // Arrange
        LegacyGetDefendantAccountEnforcementStatusResponse responseBody =
            createLegacyEnforcementStatusResponse(false);
        when(restClient.responseSpec.body(
            Mockito.<ParameterizedTypeReference<LegacyGetDefendantAccountEnforcementStatusResponse>>any()
        )).thenReturn(responseBody);
        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>(responseBody.toXml(), HttpStatus.SERVICE_UNAVAILABLE));
        when(courtService.getCourtById(anyLong())).thenReturn(Lite.builder().courtCode((short)123).build());

        EnforcementStatus response = legacyDefendantAccountService.getEnforcementStatus(66L);

        assertNotNull(response);
        assertTrue(response.getEmployerFlag());
        assertEquals(new BigInteger("1234567890123456789012345678901234567890"), response.getVersion());
        assertFalse(response.getIsHmrcCheckEligible());
        assertNull(response.getNextEnforcementActionData());
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetEnforcementStatus_courtNotFoundInOpalDB() {

        // Arrange
        LegacyGetDefendantAccountEnforcementStatusResponse responseBody =
            createLegacyEnforcementStatusResponse(false);
        when(restClient.responseSpec.body(
            Mockito.<ParameterizedTypeReference<LegacyGetDefendantAccountEnforcementStatusResponse>>any()
        )).thenReturn(responseBody);
        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>(responseBody.toXml(), HttpStatus.SERVICE_UNAVAILABLE));
        when(courtService.getCourtById(anyLong())).thenThrow(new EntityNotFoundException("Court not found"));

        EntityNotFoundException error = assertThrows(EntityNotFoundException.class,
            () -> legacyDefendantAccountService.getEnforcementStatus(66L));

        assertNotNull(error);
        assertEquals("Court not found", error.getMessage());
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetEnforcementStatus_ljaNotFoundInOpalDB() {

        // Arrange
        LegacyGetDefendantAccountEnforcementStatusResponse responseBody =
            createLegacyEnforcementStatusResponse(true);
        when(restClient.responseSpec.body(
            Mockito.<ParameterizedTypeReference<LegacyGetDefendantAccountEnforcementStatusResponse>>any()
        )).thenReturn(responseBody);
        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>(responseBody.toXml(), HttpStatus.SERVICE_UNAVAILABLE));
        when(courtService.getCourtById(anyLong())).thenReturn(Lite.builder().courtCode((short)123).build());
        when(ljaService.getLocalJusticeAreaById(anyShort())).thenThrow(new EntityNotFoundException("Lja not found"));

        EntityNotFoundException error = assertThrows(EntityNotFoundException.class,
            () -> legacyDefendantAccountService.getEnforcementStatus(66L));

        assertNotNull(error);
        assertEquals("Lja not found", error.getMessage());
    }

    private LegacyGetDefendantAccountEnforcementStatusResponse createLegacyEnforcementStatusResponse(boolean full) {
        return LegacyGetDefendantAccountEnforcementStatusResponse.builder()
            .accountStatusReference(
                uk.gov.hmcts.opal.dto.legacy.common.AccountStatusReference.builder()
                    .accountStatusCode("L")
                    .accountStatusDisplayName("Alive")
                    .build())
            .enforcementOverride(full ? EnforcementOverride.builder()  // Optional
                .lja(LjaReference.builder()
                    .ljaId((short)1).ljaName("England").build())
                .enforcer(EnforcerReference.builder()
                    .enforcerId(2L).enforcerName("Arthur").build())
                .enforcementOverrideResult(EnforcementOverrideResultReference.builder()
                    .enforcementOverrideResultId("AAB").enforcementOverrideResultName("AaAaBb").build())
                .build() : null)
            .enforcementOverview(EnforcementOverview.builder()
                .enforcementCourt(CourtReference.builder()
                    .courtId(3L).courtName("Bath").build())
                .collectionOrder(CollectionOrder.builder()
                    .collectionOrderCode("XX").collectionOrderFlag(true)
                    .collectionOrderDate(LocalDate.of(2024, 3,4)).build())
                .daysInDefault(6)
                .build())
            .lastEnforcementAction(full ? EnforcementAction.builder() // Optional
                .enforcer(EnforcerReference.builder()
                    .enforcerId(4L).enforcerName("Merlin").build())
                .resultReference(ResultReference.builder()
                    .resultId("FEE").resultTitle("Result Ref").build())
                .resultResponses(ResultResponses.builder()
                    .parameterName("Param Name").response("A response").build())
                .dateAdded("2024-01-01T10:00:00")
                .reason("late")
                .warrantNumber("123")
                .build() : null)
            .version("1234567890123456789012345678901234567890")
            .employerFlag("true")
            .build();
    }
}
