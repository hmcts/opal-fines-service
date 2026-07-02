package uk.gov.hmcts.opal.mapper.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.opal.dto.common.BusinessUnitSummary;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorAccountHeaderEntity;
import uk.gov.hmcts.opal.generated.model.BusinessUnitSummaryCommon;

class BusinessUnitSummaryMapperTest {

    private final BusinessUnitSummaryMapper mapper = Mappers.getMapper(BusinessUnitSummaryMapper.class);

    @Test
    void givenWelshLanguageTrue_whenToBusinessUnitSummary_thenMapsExpectedFields() {

        //Arrange
        MinorCreditorAccountHeaderEntity entity = MinorCreditorAccountHeaderEntity.builder()
            .businessUnitId((short) 77)
            .businessUnitName("Camberwell Green")
            .welshLanguage(true)
            .build();

        //Act
        BusinessUnitSummary mapped = mapper.toBusinessUnitSummary(entity);

        //Assert
        assertNotNull(mapped);
        assertEquals("77", mapped.getBusinessUnitId());
        assertEquals("Camberwell Green", mapped.getBusinessUnitName());
        assertEquals("Y", mapped.getWelshSpeaking());
    }

    @Test
    void givenWelshLanguageFalse_whenToBusinessUnitSummary_thenMapsWelshSpeakingN() {

        //Assert
        MinorCreditorAccountHeaderEntity entity = MinorCreditorAccountHeaderEntity.builder()
            .businessUnitId((short) 10)
            .businessUnitName("Derbyshire")
            .welshLanguage(false)
            .build();

        //Act
        BusinessUnitSummary mapped = mapper.toBusinessUnitSummary(entity);

        //Assert
        assertNotNull(mapped);
        assertEquals("10", mapped.getBusinessUnitId());
        assertEquals("Derbyshire", mapped.getBusinessUnitName());
        assertEquals("N", mapped.getWelshSpeaking());
    }

    @Test
    void toBusinessUnitSummaryCommonList() {
        List<BusinessUnitEntity> bus = List.of(
            BusinessUnitEntity.builder()
                .businessUnitId((short) 13)
                .businessUnitName("BU_13")
                .welshLanguage(true)
                .build(),
            BusinessUnitEntity.builder()
                .businessUnitId((short) 41)
                .businessUnitName("BU_41")
                .welshLanguage(false)
                .build());

        List<BusinessUnitSummaryCommon> businessUnitSummaryCommonList = mapper.toBusinessUnitSummaryCommonList(bus);

        assertNotNull(businessUnitSummaryCommonList);
        assertEquals(2, businessUnitSummaryCommonList.size());

        assertEquals("13", businessUnitSummaryCommonList.getFirst().getBusinessUnitId());
        assertEquals("BU_13", businessUnitSummaryCommonList.getFirst().getBusinessUnitName());
        assertEquals("Y", businessUnitSummaryCommonList.getFirst().getWelshSpeaking());

        assertEquals("41", businessUnitSummaryCommonList.get(1).getBusinessUnitId());
        assertEquals("BU_41", businessUnitSummaryCommonList.get(1).getBusinessUnitName());
        assertEquals("N", businessUnitSummaryCommonList.get(1).getWelshSpeaking());
    }
}
