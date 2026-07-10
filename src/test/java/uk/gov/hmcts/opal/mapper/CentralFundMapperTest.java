package uk.gov.hmcts.opal.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigInteger;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.opal.dto.CentralFundResponse;
import uk.gov.hmcts.opal.generated.model.BusinessUnitSummaryCommon;
import uk.gov.hmcts.opal.generated.model.GetCentralFundResponse;
import uk.gov.hmcts.opal.generated.model.GetCentralFundResponseMajorCreditor;
import uk.gov.hmcts.opal.repository.CentralFundProjection;

class CentralFundMapperTest {

    private final CentralFundMapper mapper = Mappers.getMapper(CentralFundMapper.class);

    @Test
    void toCentralFundResponse_whenProjectionHasValues_mapsResponse() {
        CentralFundResponse response = mapper.toCentralFundResponse(centralFundProjection(true, 7L));

        GetCentralFundResponse payload = response.getPayload();
        GetCentralFundResponseMajorCreditor majorCreditor = payload.getMajorCreditor();

        assertEquals(123L, majorCreditor.getCreditorAccountId());
        assertEquals("CF123", majorCreditor.getAccountNumber());
        assertEquals("Central Fund", majorCreditor.getName());

        BusinessUnitSummaryCommon businessUnitDetails = payload.getBusinessUnitDetails();
        assertEquals((short) 70, businessUnitDetails.getBusinessUnitId());
        assertEquals("London Collection", businessUnitDetails.getBusinessUnitName());
        assertEquals("Y", businessUnitDetails.getWelshSpeaking());
        assertEquals(BigInteger.valueOf(7), response.getVersion());
    }

    @Test
    void toCentralFundResponse_whenWelshLanguageIsNotTrue_mapsWelshSpeakingAsN() {
        CentralFundResponse response = mapper.toCentralFundResponse(centralFundProjection(null, null));

        assertEquals("N", response.getPayload().getBusinessUnitDetails().getWelshSpeaking());
        assertNull(response.getVersion());
    }

    private CentralFundProjection centralFundProjection(Boolean welshLanguage, Long versionNumber) {
        return new CentralFundProjection() {
            @Override
            public Long getCreditorAccountId() {
                return 123L;
            }

            @Override
            public String getAccountNumber() {
                return "CF123";
            }

            @Override
            public String getName() {
                return "Central Fund";
            }

            @Override
            public Short getBusinessUnitId() {
                return (short) 70;
            }

            @Override
            public String getBusinessUnitName() {
                return "London Collection";
            }

            @Override
            public Boolean getWelshLanguage() {
                return welshLanguage;
            }

            @Override
            public Long getVersionNumber() {
                return versionNumber;
            }
        };
    }
}
