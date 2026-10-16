package uk.gov.hmcts.opal.service.report;

import lombok.Getter;
import uk.gov.hmcts.opal.entity.MappingValue;

@Getter
public enum FileType implements MappingValue {
    CSV("CSV", "text/csv"),
    PDF("PDF", "application/pdf"),
    JSON("JSON", "application/json"),
    XML("XML", "application/xml");

    private final String displayName;
    private final String mimeType;

    FileType(String displayName, String mimeType) {
        this.displayName = displayName;
        this.mimeType = mimeType;
    }

    @Override
    public String getCode() {
        return this.name();
    }
}
