package uk.gov.hmcts.opal.service.report;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReportParameterData {
    private String name;
    private String prompt;
    private String type;
    private boolean mandatory;
    private Object min;
    private Object max;

    @JsonProperty("language_dependent")
    private String languageDependent;
    private String hint;

    private List<String> options;
    private String apidata;
}
