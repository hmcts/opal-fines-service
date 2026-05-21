package uk.gov.hmcts.opal.mapper.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigInteger;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.opal.dto.legacy.RemoveDefendantAccountPartyLegacyResponse;
import uk.gov.hmcts.opal.dto.response.RemoveDefendantAccountPartyResponse;

class RemoveDefendantAccountPartyLegacyResponseMapperTest {

    private final RemoveDefendantAccountPartyLegacyResponseMapper mapper =
        Mappers.getMapper(RemoveDefendantAccountPartyLegacyResponseMapper.class);

    @Test
    void toRemoveDefendantAccountPartyResponse_mapsFields() {
        RemoveDefendantAccountPartyLegacyResponse legacyResponse =
            RemoveDefendantAccountPartyLegacyResponse.builder()
                .version(BigInteger.valueOf(3))
                .defendantAccountPartyId("20010")
                .build();

        RemoveDefendantAccountPartyResponse response =
            mapper.toRemoveDefendantAccountPartyResponse(legacyResponse);

        assertNotNull(response);
        assertEquals(BigInteger.valueOf(3), response.getVersion());
        assertEquals("20010", response.getDefendantAccountPartyId());
    }

    @Test
    void toRemoveDefendantAccountPartyResponse_whenLegacyResponseIsNull_returnsEmptyResponse() {
        RemoveDefendantAccountPartyResponse response =
            mapper.toRemoveDefendantAccountPartyResponse(null);

        assertNotNull(response);
        assertNull(response.getVersion());
        assertNull(response.getDefendantAccountPartyId());
    }
}
