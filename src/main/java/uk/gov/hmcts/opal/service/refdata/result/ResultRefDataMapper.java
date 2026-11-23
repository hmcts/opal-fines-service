package uk.gov.hmcts.opal.service.refdata.result;

import java.util.ArrayList;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import uk.gov.hmcts.opal.entity.result.ImpositionCategory;
import uk.gov.hmcts.opal.entity.result.ImpositionCreditor;
import uk.gov.hmcts.opal.entity.result.ResultOpalEntity;
import uk.gov.hmcts.opal.entity.result.ResultOpalEntity;

@Mapper(componentModel = "spring")
public interface ResultRefDataMapper {

    String LANGUAGE_DEPENDENT = "languageDependent";
    String LANGUAGE_DEPENDENT_FRONTEND = "language_dependent";

    @Mapping(target = "resultId", ignore = true)
    @Mapping(target = "enforcements", ignore = true)
    @Mapping(target = "resultTitleCy", source = "resultTitleWelsh")
    @Mapping(target = "active", expression = "java(isActive(dto.getPublishingStatus()))")
    @Mapping(target = "impositionCategory", expression = "java(category(dto.getImpositionCategory()))")
    @Mapping(target = "impositionCreditor", expression = "java(creditor(dto.getImpositionCreditor()))")
    @Mapping(target = "furtherEnforcementWarn", source = "furtherEnforcementWarningRequired")
    @Mapping(target = "furtherEnforcementDisallow", source = "furtherEnforcementDisallowed")
    @Mapping(target = "enforcementHold", source = "enforcementOnHold")
    @Mapping(target = "generatesHearing", source = "generatesAHearing")
    @Mapping(target = "generatesWarrant", source = "generatesAWarrant")
    @Mapping(target = "extendTtpDisallow", source = "disallowTtpExtension")
    @Mapping(target = "extendTtpPreserveLastEnf", source = "preserveLastEnforcementOnTtpExtension")
    @Mapping(target = "resultParameters", expression = "java(parameters(dto.getResultParameters()))")
    @Mapping(target = "enfNextPermittedActions", expression = "java(actions(dto))")
    void updateEntityFromDto(ResultsRecord dto, @MappingTarget ResultOpalEntity entity);

    default boolean isActive(String status) {
        return "Active".equals(status);
    }

    @Named("category")
    default ImpositionCategory category(String category) {
        // Fund is valid in the reference schema but absent from the database enum. Store null
        // until the database supports it, rather than assigning a misleading category.
        return category == null || "Fund".equals(category) ? null : ImpositionCategory.getByLabel(category);
    }

    default ImpositionCreditor creditor(String creditor) {
        if (creditor == null) {
            return null;
        }
        return switch (creditor) {
            case "DPP" -> ImpositionCreditor.CPS;
            case "!DPP" -> ImpositionCreditor.NOT_CPS;
            case "CF", "Any" -> ImpositionCreditor.getByLabel(creditor);
            default -> throw new IllegalArgumentException("Unknown imposition creditor: " + creditor);
        };
    }

    default String parameters(JsonNode parameters) {
        if (parameters == null || parameters.isNull()) {
            return null;
        }
        if (!parameters.isArray()) {
            throw new IllegalArgumentException("result_parameters must be an array or null");
        }
        JsonNode copy = parameters.deepCopy();
        for (JsonNode parameter : copy) {
            if (!parameter.isObject()) {
                throw new IllegalArgumentException("Each result parameter must be an object");
            }
            ObjectNode object = (ObjectNode) parameter;
            if (object.has(LANGUAGE_DEPENDENT_FRONTEND)) {
                if (object.has(LANGUAGE_DEPENDENT)) {
                    throw new IllegalArgumentException("Both language_dependent and languageDependent are present");
                }
                object.set(LANGUAGE_DEPENDENT, object.remove(LANGUAGE_DEPENDENT_FRONTEND));
            }
        }
        return copy.toString();
    }

    default String actions(ResultsRecord dto) {
        if ("ALL".equals(dto.getNextPermittedActionsType())) {
            return "All";
        }
        List<String> actions = new ArrayList<>();
        if (dto.getResultNextPermittedActionMappings() != null) {
            dto.getResultNextPermittedActionMappings().forEach(action -> {
                JsonNode code = action.path("result_code");
                if (code.isTextual()) {
                    actions.add(code.asString());
                }
            });
        }
        return String.join(",", actions);
    }
}
