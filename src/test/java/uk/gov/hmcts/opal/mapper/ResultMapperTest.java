package uk.gov.hmcts.opal.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.opal.dto.reference.ResultReferenceData;
import uk.gov.hmcts.opal.entity.ImpositionCategoriesEntity;
import uk.gov.hmcts.opal.entity.result.ImpositionCreditor;
import uk.gov.hmcts.opal.entity.result.ResultEntity;
import uk.gov.hmcts.opal.entity.result.ResultType;
import uk.gov.hmcts.opal.generated.model.GetResultByIdResponseResults;
import uk.gov.hmcts.opal.generated.model.ResultsRefDataResponse;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ResultMapperTest {

    private final ResultMapper resultMapper = Mappers.getMapper(ResultMapper.class);

    @Test
    void toRefData_shouldMapLiteEntityToDto() {
        // Arrange
        ResultEntity entity = ResultEntity.builder()
            .resultId("R123")
            .resultTitle("Test Result")
            .resultTitleCy("Test Result Welsh")
            .resultType(ResultType.ACTION)
            .active(true)
            .impositionAllocationPriority((short) 1)
            .impositionCreditor(ImpositionCreditor.CF)
            .build();

        // Act
        ResultReferenceData result = resultMapper.toRefData(entity);

        // Assert
        assertNotNull(result);
        assertEquals("R123", result.getResultId());
        assertEquals("Test Result", result.getResultTitle());
        assertEquals("Test Result Welsh", result.getResultTitleCy());
        assertEquals("Action", result.getResultType());
        assertEquals(true, result.isActive());
        assertEquals((short) 1, result.getImpositionAllocationPriority());
        assertEquals("CF", result.getImpositionCreditor());
    }

    @Test
    void toRefDataFromFull_shouldMapFullEntityToDto() {
        // Arrange
        ImpositionCategoriesEntity finesCategory = ImpositionCategoriesEntity.builder()
            .impositionCategory("Fines")
            .build();

        ResultEntity entity = ResultEntity.builder()
            .resultId("R456")
            .resultTitle("Full Result")
            .resultTitleCy("Full Result Welsh")
            .resultType(ResultType.RESULT)
            .active(true)
            .impositionAllocationPriority((short) 2)
            .impositionCreditor(ImpositionCreditor.ANY)
            // Include additional fields present only in Full entity
            .imposition(true)
            .impositionCategory(finesCategory)
            .impositionAccruing(false)
            .build();

        // Act
        ResultReferenceData result = resultMapper.toRefData(entity);

        // Assert
        assertNotNull(result);
        assertEquals("R456", result.getResultId());
        assertEquals("Full Result", result.getResultTitle());
        assertEquals("Full Result Welsh", result.getResultTitleCy());
        assertEquals("Result", result.getResultType());
        assertEquals(true, result.isActive());
        assertEquals((short) 2, result.getImpositionAllocationPriority());
        assertEquals("Any", result.getImpositionCreditor());
    }

    @Test
    void toReferenceDataResponse_shouldConvertListToResponse() {
        // Arrange
        ResultEntity entity1 = ResultEntity.builder()
            .resultId("R1")
            .resultTitle("Result 1")
            .resultTitleCy("Result 1 Welsh")
            .resultType(ResultType.ACTION)
            .active(true)
            .build();

        ResultEntity entity2 = ResultEntity.builder()
            .resultId("R2")
            .resultTitle("Result 2")
            .resultTitleCy("Result 2 Welsh")
            .resultType(ResultType.RESULT)
            .active(false)
            .build();

        List<ResultEntity> entities = List.of(entity1, entity2);

        // Act
        ResultsRefDataResponse response = resultMapper.toReferenceDataResponse(entities);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getRefData());
        assertEquals(2, response.getRefData().size());
        assertEquals("R1", response.getRefData().get(0).getResultId().get());
        assertEquals("Result 1", response.getRefData().get(0).getResultTitle().get());
        assertEquals("R2", response.getRefData().get(1).getResultId().get());
        assertEquals("Result 2", response.getRefData().get(1).getResultTitle().get());
    }

    @Test
    void toDto_mapsFullEntityToGeneratedResponse() {
        // Arrange
        ImpositionCategoriesEntity compensationCategory = ImpositionCategoriesEntity.builder()
            .impositionCategory("Compensation")
            .build();

        ResultEntity entity = ResultEntity.builder()
            .resultId("R999")
            .resultTitle("Full Title")
            .resultTitleCy("Teitl Llawn")
            .resultType(ResultType.RESULT)
            .active(true)
            .impositionAllocationPriority((short) 3)
            .impositionCreditor(ImpositionCreditor.CF)
            .imposition(true)
            .impositionCategory(compensationCategory)
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
            .requiresEmploymentData(true)
            .allowPaymentTerms(false)
            .allowAdditionalAction(true)
            .generatesWarrant(true)
            .requiresLja(false)
            .manualEnforcement(true)
            .enfNextPermittedActions("ACTION1,ACTION2")
            .build();

        // Act
        GetResultByIdResponseResults dto = resultMapper.toDto(entity);

        // Assert — every field 1:1
        assertNotNull(dto);
        assertEquals(entity.getResultId(), dto.getResultId());
        assertEquals(entity.getResultTitle(), dto.getResultTitle());
        assertEquals(entity.getResultTitleCy(), dto.getResultTitleCy());
        assertEquals(entity.getResultType().getLabel(), dto.getResultType());
        assertEquals(entity.isActive(), dto.getActive());
        assertEquals(entity.getImpositionAllocationPriority(), dto.getImpositionAllocationPriority());
        assertEquals(entity.getImpositionCreditor().getLabel(), dto.getImpositionCreditor());
        assertEquals(entity.isImposition(), dto.getImposition());
        assertEquals("Compensation", dto.getImpositionCategory());
        assertEquals(entity.getImpositionAccruing(), dto.getImpositionAccruing());
        assertEquals(entity.isEnforcement(), dto.getEnforcement());
        assertEquals(entity.isEnforcementOverride(), dto.getEnforcementOverride());
        assertEquals(entity.isFurtherEnforcementWarn(), dto.getFurtherEnforcementWarn());
        assertEquals(entity.isFurtherEnforcementDisallow(), dto.getFurtherEnforcementDisallow());
        assertEquals(entity.isEnforcementHold(), dto.getEnforcementHold());
        assertEquals(entity.isRequiresEnforcer(), dto.getRequiresEnforcer());
        assertEquals(entity.isGeneratesHearing(), dto.getGeneratesHearing());
        assertEquals(entity.isCollectionOrder(), dto.getCollectionOrder());
        assertEquals(entity.isExtendTtpDisallow(), dto.getExtendTtpDisallow());
        assertEquals(entity.isExtendTtpPreserveLastEnf(), dto.getExtendTtpPreserveLastEnf());
        assertEquals(entity.isPreventPaymentCard(), dto.getPreventPaymentCard());
        assertEquals(entity.isListsMonies(), dto.getListsMonies());
        assertEquals(entity.getResultParameters(), dto.getResultParameters());
        assertEquals(entity.getRequiresEmploymentData(), dto.getRequiresEmploymentData());
        assertEquals(entity.getAllowPaymentTerms(), dto.getAllowPaymentTerms());
        assertEquals(entity.getAllowAdditionalAction(), dto.getAllowAdditionalAction());
        assertEquals(entity.isGeneratesWarrant(), dto.getGeneratesWarrant());
        assertEquals(entity.getRequiresLja(), dto.getRequiresLja());
        assertEquals(entity.isManualEnforcement(), dto.getManualEnforcement());
        assertEquals(entity.getEnfNextPermittedActions(), dto.getEnfNextPermittedActions());
    }

}
