package uk.gov.hmcts.opal.service.report;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import uk.gov.hmcts.opal.exception.ReportNotFoundException;

@Getter
public enum ReportParameterType {
    BOOLEAN("boolean"),
    DATE("date"),
    DECIMAL("decimal-2dp"),
    INTEGER("integer"),
    MENU_RADIO("menu-radio"),
    MENU_AUTOCOMPLETE("menu-autocomplete"),
    MENU_CHECKBOX("menu-checkbox"),
    TEXT_MAX_60("text-60"),
    TEXT_MAX_100("text-100"),
    TEXT_MAX_1000("text-1000");


    public final String typeName;

    ReportParameterType(String typeName) {
        this.typeName = typeName;
    }

    private static final Map<String, ReportParameterType> BY_TYPE_NAME =
        Stream.of(values())
            .collect(Collectors.toMap(ReportParameterType::getTypeName, reportParameterType -> reportParameterType));

    public static ReportParameterType fromParameterName(String typeName) {
        ReportParameterType type = BY_TYPE_NAME.get(typeName);
        if (type == null) {
            throw new ReportNotFoundException("Report Parameter name is not a valid parameter type: " + typeName);
        }
        return type;
    }
}
