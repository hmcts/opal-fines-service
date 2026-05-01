package uk.gov.hmcts.opal.documents.docmosis;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class DocmosisRenderDto {

    @JsonProperty("templateName")
    private String templateName;
    @JsonProperty("outputName")
    private String outputName;
    @JsonProperty("accessKey")
    private String accessKey;
    @JsonProperty("data")
    private Object data;
}