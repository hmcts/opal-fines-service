package uk.gov.hmcts.opal.service.legacy;

import static org.assertj.core.api.Assertions.assertThat;
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
import uk.gov.hmcts.opal.generated.model.FixedPenaltyTicketDetailsCommonStrict;
import uk.gov.hmcts.opal.generated.model.GetDefendantAccountFixedPenaltyResponse;
import uk.gov.hmcts.opal.generated.model.VehicleFixedPenaltyDetailsCommonStrict;

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

        assertThat(response).isNotNull();
        assertThat(response.getVehicleFixedPenaltyFlag()).isTrue();
        assertThat(response.getFixedPenaltyTicketDetails()).isNull();
        assertThat(response.getVehicleFixedPenaltyDetails()).isNotNull();

        VehicleFixedPenaltyDetailsCommonStrict vehiclePenaltyDetails = response.getVehicleFixedPenaltyDetails().get();
        assertThat(vehiclePenaltyDetails.getVehicleRegistrationNumber().get()).isEqualTo("ABC123");
        assertThat(vehiclePenaltyDetails.getVehicleDriversLicense().get()).isEqualTo("123456");
        assertThat(vehiclePenaltyDetails.getDateNoticeIssued().get()).isEqualTo("2023-09-09");

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

        assertThat(response).isNotNull();
        assertThat(response.getVehicleFixedPenaltyFlag()).isFalse();
        assertThat(response.getVehicleFixedPenaltyDetails().get()).isNull();
        assertThat(response.getFixedPenaltyTicketDetails()).isNotNull();

        FixedPenaltyTicketDetailsCommonStrict penaltyTicketDetails = response.getFixedPenaltyTicketDetails();
        assertThat(penaltyTicketDetails.getIssuingAuthority().get()).isEqualTo("HMCTS");
        assertThat(penaltyTicketDetails.getTicketNumber().get()).isEqualTo("123456");
        assertThat(penaltyTicketDetails.getTimeOfOffence().get()).isEqualTo("2023-09-09");
        assertThat(penaltyTicketDetails.getPlaceOfOffence().get()).isEqualTo("London");

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
