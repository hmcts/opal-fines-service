package uk.gov.hmcts.opal.dto.legacy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.legacy.common.CollectionOrder;
import uk.gov.hmcts.opal.dto.legacy.common.CommentsAndNotes;
import uk.gov.hmcts.opal.dto.legacy.common.EnforcementOverride;
import uk.gov.hmcts.opal.dto.legacy.utils.ValidationUtils;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = false)
@JsonInclude(JsonInclude.Include.NON_NULL)
@XmlAccessorType(XmlAccessType.FIELD)
public class LegacyUpdateDefendantAccountRequest {

    @JsonProperty("defendant_account_id")
    @XmlElement(name = "defendant_account_id")
    @NotNull
    private String defendantAccountId;

    @JsonProperty("business_unit_id")
    @XmlElement(name = "business_unit_id")
    @NotNull
    @Size(min = 1)
    private String businessUnitId;

    @JsonProperty("business_unit_user_id")
    @XmlElement(name = "business_unit_user_id")
    @NotNull
    @Size(min = 1)
    private String businessUnitUserId;

    @JsonProperty("version")
    @XmlElement(name = "version")
    @NotNull
    private Integer version;

    @JsonProperty("comment_and_notes")
    @XmlElement(name = "comment_and_notes")
    private CommentsAndNotes commentAndNotes;

    @JsonProperty("enforcement_court_id")
    @XmlElement(name = "enforcement_court_id")
    @Size(min = 1)
    private String enforcementCourtId;

    @JsonProperty("collection_order")
    @XmlElement(name = "collection_order")
    private CollectionOrder collectionOrder;

    @JsonProperty("enforcement_override")
    @XmlElement(name = "enforcement_override")
    private EnforcementOverride enforcementOverride;

    @AssertTrue(message = "Exactly one of comment_and_notes, enforcement_court_id, collection_order "
        + "or enforcement_override must be present")
    private boolean isExactlyOneUpdateFieldPresent() {
        return ValidationUtils.hasExactlyOneNonNull(commentAndNotes,
            enforcementCourtId,
            collectionOrder,
            enforcementOverride);
    }
}
