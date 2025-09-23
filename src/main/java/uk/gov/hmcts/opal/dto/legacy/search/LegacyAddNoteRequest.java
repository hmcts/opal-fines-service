package uk.gov.hmcts.opal.dto.legacy.search;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Builder;
import lombok.Data;

@Data
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Builder
public class LegacyAddNoteRequest {

    @NotBlank
    @XmlElement(name = "business_unit_id", required = true)
    private String businessUnitId;

    @NotBlank
    @XmlElement(name = "business_unit_user_id", required = true)
    private String businessUnitUserId;

    @NotNull
    @XmlElement(name = "version", required = true)
    private Integer version;

    @NotNull
    @Valid
    @XmlElement(name = "activity_note", required = true)
    private LegacyNote activityNote;
}
