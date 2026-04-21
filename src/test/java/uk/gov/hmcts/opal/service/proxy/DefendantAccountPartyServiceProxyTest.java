package uk.gov.hmcts.opal.service.proxy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPartyResponse;
import uk.gov.hmcts.opal.dto.common.DefendantAccountParty;
import uk.gov.hmcts.opal.dto.response.RemoveDefendantAccountPartyResponse;
import uk.gov.hmcts.opal.service.legacy.LegacyDefendantAccountPartyService;
import uk.gov.hmcts.opal.service.opal.OpalDefendantAccountPartyService;

@ExtendWith(MockitoExtension.class)
class DefendantAccountPartyServiceProxyTest extends ProxyTestsBase {

    @Mock
    private OpalDefendantAccountPartyService opalDefendantAccountPartyService;

    @Mock
    private LegacyDefendantAccountPartyService legacyDefendantAccountPartyService;


    @InjectMocks
    private DefendantAccountPartyServiceProxy proxy;

    @Test
    void replaceDefendantAccountParty_whenOpalMode_delegatesToOpalService() {
        // given
        Long accountId = 1L;
        Long dapId = 5L;
        String businessUnitId = "10";
        String postedBy = "user@example.com";
        String businessUserId = "USER123";
        String ifMatch = "1";

        DefendantAccountParty request = DefendantAccountParty.builder().build();

        GetDefendantAccountPartyResponse expectedResponse = GetDefendantAccountPartyResponse.builder()
            .version(BigInteger.valueOf(2L))
            .defendantAccountParty(request)
            .build();

        setMode(OPAL);
        when(opalDefendantAccountPartyService.replaceDefendantAccountParty(
            accountId, dapId, request, ifMatch, businessUnitId, postedBy, businessUserId))
            .thenReturn(expectedResponse);

        // when
        GetDefendantAccountPartyResponse result = proxy.replaceDefendantAccountParty(
            accountId, dapId, request, ifMatch, businessUnitId, postedBy, businessUserId);

        // then
        assertThat(result).isEqualTo(expectedResponse);
        verify(opalDefendantAccountPartyService).replaceDefendantAccountParty(
            accountId, dapId, request, ifMatch, businessUnitId, postedBy, businessUserId);
    }

    @Test
    void replaceDefendantAccountParty_whenLegacyMode_delegatesToLegacyService() {
        // given
        Long accountId = 2L;
        Long dapId = 10L;
        String businessUnitId = "20";
        String postedBy = "admin@example.com";
        String businessUserId = "ADMIN456";
        String ifMatch = "2";

        DefendantAccountParty request = DefendantAccountParty.builder().build();

        GetDefendantAccountPartyResponse expectedResponse = GetDefendantAccountPartyResponse.builder()
            .version(BigInteger.valueOf(3L))
            .defendantAccountParty(request)
            .build();

        setMode(LEGACY);
        when(legacyDefendantAccountPartyService.replaceDefendantAccountParty(
            accountId, dapId, request, ifMatch, businessUnitId, postedBy, businessUserId))
            .thenReturn(expectedResponse);

        // when
        GetDefendantAccountPartyResponse result = proxy.replaceDefendantAccountParty(
            accountId, dapId, request, ifMatch, businessUnitId, postedBy, businessUserId);

        // then
        assertThat(result).isEqualTo(expectedResponse);
        verify(legacyDefendantAccountPartyService).replaceDefendantAccountParty(
            accountId, dapId, request, ifMatch, businessUnitId, postedBy, businessUserId);
    }

    @Test
    void getDefendantAccountParty_whenOpalMode_delegatesToOpalService() {
        // given
        Long accountId = 1L;
        Long dapId = 5L;

        GetDefendantAccountPartyResponse expectedResponse = GetDefendantAccountPartyResponse.builder()
            .version(BigInteger.valueOf(1L))
            .build();

        setMode(OPAL);
        when(opalDefendantAccountPartyService.getDefendantAccountParty(accountId, dapId))
            .thenReturn(expectedResponse);

        // when
        GetDefendantAccountPartyResponse result = proxy.getDefendantAccountParty(accountId, dapId);

        // then
        assertThat(result).isEqualTo(expectedResponse);
        verify(opalDefendantAccountPartyService).getDefendantAccountParty(accountId, dapId);
    }

    @Test
    void getDefendantAccountParty_whenLegacyMode_delegatesToLegacyService() {
        // given
        Long accountId = 2L;
        Long dapId = 10L;

        GetDefendantAccountPartyResponse expectedResponse = GetDefendantAccountPartyResponse.builder()
            .version(BigInteger.valueOf(2L))
            .build();

        setMode(LEGACY);
        when(legacyDefendantAccountPartyService.getDefendantAccountParty(accountId, dapId))
            .thenReturn(expectedResponse);

        // when
        GetDefendantAccountPartyResponse result = proxy.getDefendantAccountParty(accountId, dapId);

        // then
        assertThat(result).isEqualTo(expectedResponse);
        verify(legacyDefendantAccountPartyService).getDefendantAccountParty(accountId, dapId);
    }

    @Test
    void removeDefendantAccountParty_whenOpalMode_delegatesToOpalService() {
        // given
        Long accountId = 1L;
        Long dapId = 5L;
        Short businessUnitId = 10;
        String businessUserId = "USER123";
        String ifMatch = "1";
        String postedBy = "user@example.com";
        DefendantAccountParty request = DefendantAccountParty.builder().build();

        RemoveDefendantAccountPartyResponse expectedResponse = RemoveDefendantAccountPartyResponse.builder()
            .defendantAccountPartyId("5")
            .version(BigInteger.valueOf(2L))
            .build();

        setMode(OPAL);
        when(opalDefendantAccountPartyService.removeDefendantAccountParty(
            accountId, dapId, businessUnitId, businessUserId, ifMatch, postedBy, request))
            .thenReturn(expectedResponse);

        // when
        RemoveDefendantAccountPartyResponse result = proxy.removeDefendantAccountParty(
            accountId, dapId, businessUnitId, businessUserId, ifMatch, postedBy, request);

        // then
        assertThat(result).isEqualTo(expectedResponse);
        verify(opalDefendantAccountPartyService).removeDefendantAccountParty(
            accountId, dapId, businessUnitId, businessUserId, ifMatch, postedBy, request);
    }

    @Test
    void removeDefendantAccountParty_whenLegacyMode_delegatesToLegacyService() {
        // given
        Long accountId = 2L;
        Long dapId = 10L;
        Short businessUnitId = 20;
        String businessUserId = "ADMIN456";
        String ifMatch = "2";
        String postedBy = "admin@example.com";
        DefendantAccountParty request = DefendantAccountParty.builder().build();

        RemoveDefendantAccountPartyResponse expectedResponse = RemoveDefendantAccountPartyResponse.builder()
            .defendantAccountPartyId("10")
            .version(BigInteger.valueOf(3L))
            .build();

        setMode(LEGACY);
        when(legacyDefendantAccountPartyService.removeDefendantAccountParty(
            accountId, dapId, businessUnitId, businessUserId, ifMatch, postedBy, request))
            .thenReturn(expectedResponse);

        // when
        RemoveDefendantAccountPartyResponse result = proxy.removeDefendantAccountParty(
            accountId, dapId, businessUnitId, businessUserId, ifMatch, postedBy, request);

        // then
        assertThat(result).isEqualTo(expectedResponse);
        verify(legacyDefendantAccountPartyService).removeDefendantAccountParty(
            accountId, dapId, businessUnitId, businessUserId, ifMatch, postedBy, request);
    }
}
