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
public class DocumentSearchDto implements ToJsonString {

    private String documentId;
    private String recipient;
    private String documentLanguage;
    private String signatureSource;
    private String headerType;
    private String documentElements;

}
