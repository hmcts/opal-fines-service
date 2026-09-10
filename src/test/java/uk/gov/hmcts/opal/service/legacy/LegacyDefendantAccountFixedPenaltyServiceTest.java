package uk.gov.hmcts.opal.service.legacy;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.opal.common.legacy.service.GatewayService;
import uk.gov.hmcts.opal.dto.legacy.FixedPenaltyTicketDetails;
import uk.gov.hmcts.opal.dto.legacy.LegacyDefendantAccountGetFixedPenaltyRequest;
import uk.gov.hmcts.opal.dto.legacy.LegacyDefendantAccountGetFixedPenaltyResponse;
import uk.gov.hmcts.opal.dto.legacy.VehicleFixedPenaltyDetails;
import uk.gov.hmcts.opal.generated.model.GetDefendantAccountFixedPenaltyResponse;

@ExtendWith(MockitoExtension.class)
public class LegacyDefendantAccountFixedPenaltyServiceTest {

    @Mock
    private GatewayService gatewayService;

    @InjectMocks
    private LegacyDefendantAccountFixedPenaltyService legacyDefendantAccountFixedPenaltyService;

    @Test
    void getFixedPenalties_postsLegacyRequestAndMapsResponse_VehicleFixedPenalty() {
        LegacyDefendantAccountGetFixedPenaltyResponse legacyResponse =
            LegacyDefendantAccountGetFixedPenaltyResponse
                .builder()
                .vehicleFixedPenaltyFlag(true)
                .fixedPenaltyDetails(null)
                .vehicleFixedPenaltyDetails(
                    VehicleFixedPenaltyDetails
                        .builder()
                        .fpRegistrationNumber("ABC123")
                        .fpDrivingLicense("123456")
                        .noticeToOwnerOrHirerNumber("123456")
                        .dateNoticeIssued("2023-09-09")
                        .build())
                .build();

        ArgumentCaptor<LegacyDefendantAccountGetFixedPenaltyRequest> requestCaptor =
            ArgumentCaptor.forClass(LegacyDefendantAccountGetFixedPenaltyRequest.class);

        when(gatewayService.postToGateway(
            eq(LegacyDefendantAccountFixedPenaltyService.GET_FIXED_PENALTY),
            eq(LegacyDefendantAccountGetFixedPenaltyResponse.class),
            requestCaptor.capture(),
            isNull()
        )).thenReturn(new GatewayService.Response<>(HttpStatus.OK, legacyResponse, null, null));

        GetDefendantAccountFixedPenaltyResponse response =
            legacyDefendantAccountFixedPenaltyService.getDefendantAccountFixedPenalty(1234L);

        verify(gatewayService).postToGateway(
            eq(LegacyDefendantAccountFixedPenaltyService.GET_FIXED_PENALTY),
            eq(LegacyDefendantAccountGetFixedPenaltyResponse.class),
            eq(requestCaptor.getValue()),
            isNull()
        );
    }

    @Test
    void getFixedPenalties_postsLegacyRequestAndMapsResponse_NoVehicleFixedPenalty() {
        LegacyDefendantAccountGetFixedPenaltyResponse legacyResponse =
            LegacyDefendantAccountGetFixedPenaltyResponse
                .builder()
                .vehicleFixedPenaltyFlag(false)
                .fixedPenaltyDetails(FixedPenaltyTicketDetails
                    .builder()
                    .issuingAuthority("HMCTS")
                    .ticketNumber("123456")
                    .timeOfOffence("2023-09-09")
                    .placeOfOffence("London")
                    .build())
                .vehicleFixedPenaltyDetails(null)
                .build();

        ArgumentCaptor<LegacyDefendantAccountGetFixedPenaltyRequest> requestCaptor =
            ArgumentCaptor.forClass(LegacyDefendantAccountGetFixedPenaltyRequest.class);

        when(gatewayService.postToGateway(
            eq(LegacyDefendantAccountFixedPenaltyService.GET_FIXED_PENALTY),
            eq(LegacyDefendantAccountGetFixedPenaltyResponse.class),
            requestCaptor.capture(),
            isNull()
        )).thenReturn(new GatewayService.Response<>(HttpStatus.OK, legacyResponse, null, null));

        GetDefendantAccountFixedPenaltyResponse response =
            legacyDefendantAccountFixedPenaltyService.getDefendantAccountFixedPenalty(1234L);

        verify(gatewayService).postToGateway(
            eq(LegacyDefendantAccountFixedPenaltyService.GET_FIXED_PENALTY),
            eq(LegacyDefendantAccountGetFixedPenaltyResponse.class),
            eq(requestCaptor.getValue()),
            isNull()
        );
    }

    @Test
    void getFixedPenalties_rethrowsExceptionFromGateway() {
        RuntimeException gatewayException = new RuntimeException("Legacy gateway failed");

        ArgumentCaptor<LegacyDefendantAccountGetFixedPenaltyRequest> requestCaptor =
            ArgumentCaptor.forClass(LegacyDefendantAccountGetFixedPenaltyRequest.class);

        when(gatewayService.postToGateway(
            eq(LegacyDefendantAccountFixedPenaltyService.GET_FIXED_PENALTY),
            eq(LegacyDefendantAccountGetFixedPenaltyResponse.class),
            requestCaptor.capture(),
            isNull()
        )).thenThrow(gatewayException);

        RuntimeException thrown = assertThrows(
            RuntimeException.class,
            () -> legacyDefendantAccountFixedPenaltyService.getDefendantAccountFixedPenalty(1234L)
        );

        assertSame(gatewayException, thrown);
        verify(gatewayService).postToGateway(
            eq(LegacyDefendantAccountFixedPenaltyService.GET_FIXED_PENALTY),
            eq(LegacyDefendantAccountGetFixedPenaltyResponse.class),
            eq(requestCaptor.getValue()),
            isNull()
        );
    }

}
