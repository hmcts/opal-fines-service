package uk.gov.hmcts.opal.dto.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.ToJsonString;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TemplateMappingSearchDto implements ToJsonString {

    private String templateId;
    private String templateName;
    private String applicationFunctionId;

}
