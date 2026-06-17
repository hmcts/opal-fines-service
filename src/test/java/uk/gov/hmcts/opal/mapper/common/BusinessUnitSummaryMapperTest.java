package uk.gov.hmcts.opal.mapper.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.opal.dto.common.BusinessUnitSummary;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorAccountHeaderEntity;

class BusinessUnitSummaryMapperTest {

    private final BusinessUnitSummaryMapper mapper = Mappers.getMapper(BusinessUnitSummaryMapper.class);

    @Test
    void givenWelshLanguageTrue_whenToBusinessUnitSummary_thenMapsExpectedFields() {

        //Arrange
        MinorCreditorAccountHeaderEntity entity = MinorCreditorAccountHeaderEntity.builder()
            .businessUnitId((short) 77)
            .businessUnitName("Camberwell Green")
            .businessUnitCode("CBG")
            .welshLanguage(true)
            .build();

        //Act
        BusinessUnitSummary mapped = mapper.toBusinessUnitSummary(entity);

        //Assert
        assertNotNull(mapped);
        assertEquals("77", mapped.getBusinessUnitId());
        assertEquals("CBG", mapped.getBusinessUnitCode());
        assertEquals("Camberwell Green", mapped.getBusinessUnitName());
        assertEquals("Y", mapped.getWelshSpeaking());
    }

    @Test
    void givenWelshLanguageFalse_whenToBusinessUnitSummary_thenMapsWelshSpeakingN() {

        //Assert
        MinorCreditorAccountHeaderEntity entity = MinorCreditorAccountHeaderEntity.builder()
            .businessUnitId((short) 10)
            .businessUnitName("Derbyshire")
            .businessUnitCode("DBY")
            .welshLanguage(false)
            .build();

        //Act
        BusinessUnitSummary mapped = mapper.toBusinessUnitSummary(entity);

        //Assert
        assertNotNull(mapped);
        assertEquals("10", mapped.getBusinessUnitId());
        assertEquals("DBY", mapped.getBusinessUnitCode());
        assertEquals("Derbyshire", mapped.getBusinessUnitName());
        assertEquals("N", mapped.getWelshSpeaking());
    }
}
