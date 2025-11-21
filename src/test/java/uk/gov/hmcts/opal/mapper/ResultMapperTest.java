package uk.gov.hmcts.opal.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.opal.dto.reference.ResultReferenceData;
import uk.gov.hmcts.opal.dto.reference.ResultReferenceDataResponse;
import uk.gov.hmcts.opal.entity.result.ResultEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ResultMapperTest {

    private final ResultMapper resultMapper = Mappers.getMapper(ResultMapper.class);

    @Test
    void toRefData_shouldMapLiteEntityToDto() {
        // Arrange
        ResultEntity.Lite entity = ResultEntity.Lite.builder()
            .resultId("R123")
            .resultTitle("Test Result")
            .resultTitleCy("Test Result Welsh")
            .resultType("FPD")
            .active(true)
            .impositionAllocationPriority((short) 1)
            .impositionCreditor("HMCTS")
            .build();

        // Act
        ResultReferenceData result = resultMapper.toRefData(entity);

        // Assert
        assertNotNull(result);
        assertEquals("R123", result.resultId());
        assertEquals("Test Result", result.resultTitle());
        assertEquals("Test Result Welsh", result.resultTitleCy());
        assertEquals("FPD", result.resultType());
        assertEquals(true, result.active());
        assertEquals((short) 1, result.impositionAllocationPriority());
        assertEquals("HMCTS", result.impositionCreditor());
    }

    @Test
    void toRefDataFromFull_shouldMapFullEntityToDto() {
        // Arrange
        ResultEntity.Lite entity = ResultEntity.Lite.builder()
            .resultId("R456")
            .resultTitle("Full Result")
            .resultTitleCy("Full Result Welsh")
            .resultType("FPR")
            .active(true)
            .impositionAllocationPriority((short) 2)
            .impositionCreditor("COURT")
            // Include additional fields present only in Full entity
            .imposition(true)
            .impositionCategory("FINE")
            .impositionAccruing(false)
            .build();

        // Act
        ResultReferenceData result = resultMapper.toRefData(entity);

        // Assert
        assertNotNull(result);
        assertEquals("R456", result.resultId());
        assertEquals("Full Result", result.resultTitle());
        assertEquals("Full Result Welsh", result.resultTitleCy());
        assertEquals("FPR", result.resultType());
        assertEquals(true, result.active());
        assertEquals((short) 2, result.impositionAllocationPriority());
        assertEquals("COURT", result.impositionCreditor());
    }

    @Test
    void toReferenceDataResponse_shouldConvertListToResponse() {
        // Arrange
        ResultEntity.Lite entity1 = ResultEntity.Lite.builder()
            .resultId("R1")
            .resultTitle("Result 1")
            .resultTitleCy("Result 1 Welsh")
            .resultType("TYPE1")
            .active(true)
            .build();

        ResultEntity.Lite entity2 = ResultEntity.Lite.builder()
            .resultId("R2")
            .resultTitle("Result 2")
            .resultTitleCy("Result 2 Welsh")
            .resultType("TYPE2")
            .active(false)
            .build();

        List<ResultEntity.Lite> entities = List.of(entity1, entity2);

        // Act
        ResultReferenceDataResponse response = resultMapper.toReferenceDataResponse(entities);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getRefData());
        assertEquals(2, response.getRefData().size());
        assertEquals("R1", response.getRefData().get(0).resultId());
        assertEquals("Result 1", response.getRefData().get(0).resultTitle());
        assertEquals("R2", response.getRefData().get(1).resultId());
        assertEquals("Result 2", response.getRefData().get(1).resultTitle());
    }

    @Test
    void toDto_shouldMapLiteEntityToFullResultDto() {
        // Arrange — build full entity
        ResultEntity.Lite entity = ResultEntity.Lite.builder()
            .resultId("R999")
            .resultTitle("Full Title")
            .resultTitleCy("Teitl Llawn")
            .resultType("FULL")
            .active(true)
            .impositionAllocationPriority((short) 3)
            .impositionCreditor("HMCTS")
            .imposition(true)
            .impositionCategory("CAT")
            .impositionAccruing(false)
            .enforcement(true)
            .enforcementOverride(false)
            .furtherEnforcementWarn(true)
            .furtherEnforcementDisallow(false)
            .enforcementHold(false)
            .requiresEnforcer(true)
            .generatesHearing(true)
            .collectionOrder(true)
            .extendTtpDisallow(false)
            .extendTtpPreserveLastEnf(true)
            .preventPaymentCard(true)
            .listsMonies(true)
            .resultParameters("A,B,C")
            .build();

        // Act
        uk.gov.hmcts.opal.dto.ResultDto dto = resultMapper.toDto(entity);

        // Assert — every field 1:1
        assertNotNull(dto);
        assertEquals(entity.getResultId(), dto.getResultId());
        assertEquals(entity.getResultTitle(), dto.getResultTitle());
        assertEquals(entity.getResultTitleCy(), dto.getResultTitleCy());
        assertEquals(entity.getResultType(), dto.getResultType());
        assertEquals(entity.isActive(), dto.isActive());
        assertEquals(entity.getImpositionAllocationPriority(), dto.getImpositionAllocationPriority());
        assertEquals(entity.getImpositionCreditor(), dto.getImpositionCreditor());
        assertEquals(entity.isImposition(), dto.isImposition());
        assertEquals(entity.getImpositionCategory(), dto.getImpositionCategory());
        assertEquals(entity.getImpositionAccruing(), dto.getImpositionAccruing());
        assertEquals(entity.isEnforcement(), dto.isEnforcement());
        assertEquals(entity.isEnforcementOverride(), dto.isEnforcementOverride());
        assertEquals(entity.isFurtherEnforcementWarn(), dto.isFurtherEnforcementWarn());
        assertEquals(entity.isFurtherEnforcementDisallow(), dto.isFurtherEnforcementDisallow());
        assertEquals(entity.isEnforcementHold(), dto.isEnforcementHold());
        assertEquals(entity.isRequiresEnforcer(), dto.isRequiresEnforcer());
        assertEquals(entity.isGeneratesHearing(), dto.isGeneratesHearing());
        assertEquals(entity.isCollectionOrder(), dto.isCollectionOrder());
        assertEquals(entity.isExtendTtpDisallow(), dto.isExtendTtpDisallow());
        assertEquals(entity.isExtendTtpPreserveLastEnf(), dto.isExtendTtpPreserveLastEnf());
        assertEquals(entity.isPreventPaymentCard(), dto.isPreventPaymentCard());
        assertEquals(entity.isListsMonies(), dto.isListsMonies());
        assertEquals(entity.getResultParameters(), dto.getResultParameters());
    }

}
