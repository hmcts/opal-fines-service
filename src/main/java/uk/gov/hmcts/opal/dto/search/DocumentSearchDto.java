package uk.gov.hmcts.opal.dto.search;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.opal.dto.ToJsonString;

@Data
@Builder
public class DocumentSearchDto implements ToJsonString {

    private String documentId;
    private String recipient;
    private String documentLanguage;
    private String signatureSource;
    private String headerType;
    private String documentElements;

}
