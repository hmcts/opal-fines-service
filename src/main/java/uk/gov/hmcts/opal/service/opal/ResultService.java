package uk.gov.hmcts.opal.service.opal;


import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.dto.reference.ResultReferenceData;
import uk.gov.hmcts.opal.dto.search.ResultSearchDto;
import uk.gov.hmcts.opal.entity.result.ResultEntity;
import uk.gov.hmcts.opal.generated.model.GetResultByIdResponseResults;
import uk.gov.hmcts.opal.generated.model.ResultsRefDataResponse;
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
    private static final Set<String> WELSH_PARAMETER_TYPES = Set.of(
        "text",
        "text-60",
        "text-100",
        "text-1000",
        "date",
        "integer",
        "decimal",
        "menu-radio",
        "menu-checkbox",
        "menu-autocomplete"
    );

    @Transactional(readOnly = true)
    public ResultEntity getResultById(String resultId) {
        return resultRepository.findById(resultId)
            .orElseThrow(() -> new EntityNotFoundException("'Result' not found with id: " + resultId));
    }

    @Transactional(readOnly = true)
    public ResultReferenceData getResultRefDataById(String resultId) {
        return resultMapper.toRefData(getResultById(resultId));
    }

    @Cacheable(value = "resultsCache", key = "#root.method.name + '_' + #resultId + '_' + #includeWelsh")
    @Transactional(readOnly = true)
    public GetResultByIdResponseResults getResult(String resultId, boolean includeWelsh) {
        ResultEntity entity = resultRepository.findWithFullGraphByResultId(resultId)
            .orElseThrow(() -> new EntityNotFoundException("'Result' not found with id: " + resultId));

        GetResultByIdResponseResults result = resultMapper.toDto(entity);
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
            && WELSH_PARAMETER_TYPES.contains(parameter.path("type").asText())
            && parameter.path("language_dependent").asBoolean(false);
    }

    private ObjectNode createWelshParameter(JsonNode parameter) {
        ObjectNode welshParameter = (ObjectNode) parameter.deepCopy();
        welshParameter.put("name", WELSH_PARAMETER_PREFIX + parameter.path("name").asText());
        welshParameter.put("hint", WELSH_PARAMETER_HINT);
        return welshParameter;
    }

    public ResultsRefDataResponse getResultsByIds(List<String> resultIds,
        Boolean active,
        Boolean manualEnforcement,
        Boolean generatesHearing,
        Boolean enforcement,
        Boolean enforcementOverride) {

        Sort idSort = Sort.by(Sort.Direction.ASC, "resultId");
        Optional<List<String>> resultIdsFilter = Optional.ofNullable(resultIds)
            .filter(ids -> ids.stream().anyMatch(id -> !id.isBlank()));

        Page<ResultEntity> page = resultRepository.findBy(
            resultSpecs.referenceDataByIds(resultIdsFilter, active, manualEnforcement, generatesHearing,
                enforcement,
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
