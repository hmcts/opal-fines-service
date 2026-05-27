package uk.gov.hmcts.opal.service.report;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ReportParameterData(String name,
                                  String prompt,
                                  String type,
                                  boolean mandatory,
                                  Object min,
                                  Object max,
                                  @JsonProperty("language_dependent") String languageDependent,
                                  String hint,
                                  List<String> options,
                                  String apidata) {
}
