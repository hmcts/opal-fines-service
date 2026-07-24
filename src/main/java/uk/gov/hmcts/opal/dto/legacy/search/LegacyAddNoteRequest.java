package uk.gov.hmcts.opal.dto.legacy.search;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.math.BigInteger;
import lombok.Builder;
import lombok.Data;

@Data
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Builder
public class LegacyAddNoteRequest {

    @NotBlank
    @XmlElement(name = "business_unit_id", required = true)
    private Short businessUnitId;

    @NotBlank
    @XmlElement(name = "business_unit_user_id", required = true)
    private Long businessUnitUserId;

    @NotNull
    @XmlElement(name = "version", required = true)
    private BigInteger version;

    @NotNull
    @Valid
    @XmlElement(name = "activity_note", required = true)
    private LegacyNote activityNote;
}
