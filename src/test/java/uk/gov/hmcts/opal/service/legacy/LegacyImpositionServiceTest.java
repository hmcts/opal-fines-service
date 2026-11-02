package uk.gov.hmcts.opal.service.legacy;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.opal.common.legacy.service.GatewayService;
import uk.gov.hmcts.opal.dto.GetDefendantAccountImpositionsResponse;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountImpositionsLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetImpositionsRequest;
import uk.gov.hmcts.opal.generated.model.CourtReferenceCommon;
import uk.gov.hmcts.opal.generated.model.DefendantAccountImpositionCommon;
import uk.gov.hmcts.opal.generated.model.DefendantAccountImpositionsResponseCommon;
import uk.gov.hmcts.opal.mapper.legacy.DefendantAccountImpositionsLegacyResponseMapper;

@ExtendWith(MockitoExtension.class)
class LegacyImpositionServiceTest {

    @Mock
    private GatewayService gatewayService;

    @Mock
    private DefendantAccountImpositionsLegacyResponseMapper impositionsResponseMapper;

    @InjectMocks
    private LegacyImpositionService legacyImpositionService;

    @Nested
    class GetImpositions {

        @Test
        void whenGatewayReturnsLegacyResponse_delegatesToMapper_happyPath() {
            ArgumentCaptor<LegacyGetImpositionsRequest> requestCaptor =
                ArgumentCaptor.forClass(LegacyGetImpositionsRequest.class);
            GetDefendantAccountImpositionsLegacyResponse legacyResponse =
                GetDefendantAccountImpositionsLegacyResponse.builder().version(BigInteger.ONE).build();
            GetDefendantAccountImpositionsResponse mappedResponse =
                GetDefendantAccountImpositionsResponse.builder().version(BigInteger.ONE).build();

            when(gatewayService.postToGateway(
                eq(LegacyImpositionService.GET_IMPOSITIONS),
                eq(GetDefendantAccountImpositionsLegacyResponse.class),
                any(LegacyGetImpositionsRequest.class),
                isNull()
            )).thenReturn(new GatewayService.Response<>(HttpStatus.OK, legacyResponse, null, null));
            when(impositionsResponseMapper.toOpal(legacyResponse)).thenReturn(mappedResponse);

            GetDefendantAccountImpositionsResponse response = legacyImpositionService.getImpositions(12345L);

            assertAll(
                () -> verify(gatewayService).postToGateway(
                    eq(LegacyImpositionService.GET_IMPOSITIONS),
                    eq(GetDefendantAccountImpositionsLegacyResponse.class),
                    requestCaptor.capture(),
                    isNull()
                ),
                () -> verify(impositionsResponseMapper).toOpal(legacyResponse),
                () -> assertSame(mappedResponse, response)
            );
            assertEquals("12345", requestCaptor.getValue().getDefendantAccountId());
        }

        @Test
        void whenGatewayReturnsLegacyResponse_withNullImposingCourtValues_returnsMappedResponse() {
            ArgumentCaptor<LegacyGetImpositionsRequest> requestCaptor =
                ArgumentCaptor.forClass(LegacyGetImpositionsRequest.class);

            GetDefendantAccountImpositionsLegacyResponse legacyResponse =
                GetDefendantAccountImpositionsLegacyResponse.builder().version(BigInteger.ONE).build();

            GetDefendantAccountImpositionsResponse mappedResponse = GetDefendantAccountImpositionsResponse.builder()
                .version(BigInteger.ONE)
                .payload(
                    DefendantAccountImpositionsResponseCommon.builder()
                        .impositions(
                            List.of(
                                DefendantAccountImpositionCommon.builder()
                                    .imposedBy(CourtReferenceCommon.builder().build())
                                    .build()))
                        .build())
                .build();

            when(gatewayService.postToGateway(
                eq(LegacyImpositionService.GET_IMPOSITIONS),
                eq(GetDefendantAccountImpositionsLegacyResponse.class),
                any(LegacyGetImpositionsRequest.class),
                isNull()
            )).thenReturn(new GatewayService.Response<>(HttpStatus.OK, legacyResponse, null, null));
            when(impositionsResponseMapper.toOpal(legacyResponse)).thenReturn(mappedResponse);

            GetDefendantAccountImpositionsResponse response = legacyImpositionService.getImpositions(12345L);

            assertAll(
                () -> verify(gatewayService).postToGateway(
                    eq(LegacyImpositionService.GET_IMPOSITIONS),
                    eq(GetDefendantAccountImpositionsLegacyResponse.class),
                    requestCaptor.capture(),
                    isNull()
                ),
                () -> {
                    var courtRef = response.getPayload().getImpositions().getFirst().getImposedBy();
                    assertNotNull(courtRef);
                    assertNull(courtRef.getCourtId());
                    assertNull(courtRef.getCourtCode());
                    assertNull(courtRef.getCourtName());
                },
                () -> verify(impositionsResponseMapper).toOpal(legacyResponse),
                () -> assertSame(mappedResponse, response)
            );
            assertEquals("12345", requestCaptor.getValue().getDefendantAccountId());
        }

        @Test
        void whenGatewayReturnsNullEntity_delegatesNullToMapper_sadPath() {
            mock_getImpositionsResponse(null);

            assertAll(
                () -> assertNull(legacyImpositionService.getImpositions(12345L)),
                () -> verify(impositionsResponseMapper).toOpal((GetDefendantAccountImpositionsLegacyResponse) null)
            );
        }
    }

    private void mock_getImpositionsResponse(GetDefendantAccountImpositionsLegacyResponse response) {
        when(gatewayService.postToGateway(
            eq(LegacyImpositionService.GET_IMPOSITIONS),
            eq(GetDefendantAccountImpositionsLegacyResponse.class),
            eq(LegacyGetImpositionsRequest.builder().defendantAccountId("12345").build()),
            isNull()
        )).thenReturn(new GatewayService.Response<>(HttpStatus.OK, response, null, null));
    }
}
