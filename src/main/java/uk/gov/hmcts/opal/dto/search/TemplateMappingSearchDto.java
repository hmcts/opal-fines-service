package uk.gov.hmcts.opal.dto.search;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.opal.dto.ToJsonString;

@Data
@Builder
public class TemplateMappingSearchDto implements ToJsonString {

    private String templateId;
    private String applicationFunctionId;

}
