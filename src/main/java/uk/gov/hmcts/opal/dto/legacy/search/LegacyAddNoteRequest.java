package uk.gov.hmcts.opal.dto.legacy.search;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("business_unit_id")
    @XmlElement(name = "business_unit_id", required = true)
    private String businessUnitId;

    @NotBlank
    @JsonProperty("business_unit_user_id")
    @XmlElement(name = "business_unit_user_id", required = true)
    private String businessUnitUserId;

    @NotNull
    @JsonProperty("version")
    @XmlElement(name = "version", required = true)
    private BigInteger version;

    @NotNull
    @Valid
    @JsonProperty("activity_note")
    @XmlElement(name = "activity_note", required = true)
    private LegacyNote activityNote;
}
