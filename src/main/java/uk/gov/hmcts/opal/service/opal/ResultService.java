package uk.gov.hmcts.opal.service.opal;


import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import uk.gov.hmcts.opal.dto.ResultDto;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.dto.reference.ResultReferenceData;
import uk.gov.hmcts.opal.dto.reference.ResultReferenceDataResponse;
import uk.gov.hmcts.opal.dto.search.ResultSearchDto;
import uk.gov.hmcts.opal.entity.result.ResultEntity;
import uk.gov.hmcts.opal.mapper.ResultMapper;
import uk.gov.hmcts.opal.repository.ResultRepository;
import uk.gov.hmcts.opal.repository.jpa.ResultSpecs;

@Service
@RequiredArgsConstructor
@Qualifier("resultService")
public class ResultService {

    private final ResultRepository resultRepository;
    private final ResultMapper resultMapper;
    private final ResultSpecs resultSpecs;
    private static final String WELSH_PARAMETER_PREFIX = "cy_";
    private static final String WELSH_PARAMETER_HINT = "Provide a welsh version for the defendant";

    @Transactional(readOnly = true)
    public ResultEntity getResultById(String resultId) {
        return resultRepository.findById(resultId)
            .orElseThrow(() -> new EntityNotFoundException("'Result' not found with id: " + resultId));
    }

    @Transactional(readOnly = true)
    public ResultReferenceData getResultRefDataById(String resultId) {
        return resultMapper.toRefData(getResultById(resultId));
    }

    @Cacheable(value = "resultsCache", key = "#root.method.name + '_' + #resultId")
    public ResultDto getResult(String resultId) {
        return getResult(resultId, false);
    }

    public ResultDto getResult(String resultId, boolean includeWelsh) {
        ResultEntity entity = resultRepository.findById(resultId)
            .orElseThrow(() -> new EntityNotFoundException("'Result' not found with id: " + resultId));

        ResultDto result = resultMapper.toDto(entity);
        if (includeWelsh) {
            result.setResultParameters(addWelshResultParameters(result.getResultParameters()));
        }

        return result;
    }

    private String addWelshResultParameters(String resultParameters) {
        try {
            JsonNode parameters = ToJsonString.toJsonNode(resultParameters);
            if (!parameters.isArray()) {
                return resultParameters;
            }

            ArrayNode updatedParameters = ToJsonString.getObjectMapper().createArrayNode();
            for (JsonNode parameter : parameters) {
                updatedParameters.add(parameter.deepCopy());
                if (isWelshParameterRequired(parameter)) {
                    updatedParameters.add(createWelshParameter(parameter));
                }
            }

            return ToJsonString.getObjectMapper().writeValueAsString(updatedParameters);
        } catch (JacksonException e) {
            return resultParameters;
        }
    }

    private boolean isWelshParameterRequired(JsonNode parameter) {
        return parameter.isObject()
            && "text".equals(parameter.path("type").asText())
            && parameter.path("language_dependent").asBoolean(false);
    }

    private ObjectNode createWelshParameter(JsonNode parameter) {
        ObjectNode welshParameter = (ObjectNode) parameter.deepCopy();
        welshParameter.put("name", WELSH_PARAMETER_PREFIX + parameter.path("name").asText());
        welshParameter.put("hint", WELSH_PARAMETER_HINT);
        return welshParameter;
    }

    // @Cacheable(cacheNames = "resultReferenceDataByIds", key = "#resultIds.orElse('noIds'))")
    public ResultReferenceDataResponse getResultsByIds(Optional<List<String>> resultIds,
        Boolean active,
        Boolean manualEnforcement,
        Boolean generatesHearing,
        Boolean enforcement,
        Boolean enforcementOverride) {

        Sort idSort = Sort.by(Sort.Direction.ASC, "resultId");

        Page<ResultEntity> page = resultRepository.findBy(
            resultSpecs.referenceDataByIds(resultIds, active, manualEnforcement, generatesHearing, enforcement,
                enforcementOverride),
            ffq -> ffq
                .sortBy(idSort)
                .page(Pageable.unpaged())
        );

        return resultMapper.toReferenceDataResponse(page.getContent());
    }

    public List<ResultEntity> searchResults(ResultSearchDto criteria) {
        Page<ResultEntity> page = resultRepository
            .findBy(
                resultSpecs.findBySearchCriteria(criteria),
                ffq -> ffq.page(Pageable.unpaged()));

        return page.getContent();
    }

    @Cacheable(cacheNames = "resultReferenceDataCache", key = "#filter.orElse('noFilter')")
    public List<ResultReferenceData> getReferenceData(Optional<String> filter) {

        Sort nameSort = Sort.by(Sort.Direction.ASC, "resultTitle");

        Page<ResultEntity> page = resultRepository
            .findBy(
                resultSpecs.referenceDataFilter(filter),
                ffq -> ffq
                    .sortBy(nameSort)
                    .page(Pageable.unpaged()));

        return page.getContent().stream().map(resultMapper::toRefData).toList();
    }

}
