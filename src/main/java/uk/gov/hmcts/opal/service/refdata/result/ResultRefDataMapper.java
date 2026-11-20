package uk.gov.hmcts.opal.service.refdata.result;

import java.util.ArrayList;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import tools.jackson.databind.JsonNode;
import uk.gov.hmcts.opal.entity.result.ImpositionCategory;
import uk.gov.hmcts.opal.entity.result.ImpositionCreditor;
import uk.gov.hmcts.opal.entity.result.ResultEntity;

@Mapper(componentModel = "spring")
public interface ResultRefDataMapper {
    @Mapping(target = "resultId", ignore = true)
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
    void updateEntityFromDto(ResultsRecord dto, @MappingTarget ResultEntity entity);

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
        return parameters != null && parameters.isArray() ? parameters.toString() : null;
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
