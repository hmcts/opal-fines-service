package uk.gov.hmcts.opal.dto.legacy;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.ToXmlString;
import uk.gov.hmcts.opal.dto.legacy.common.CollectionOrder;
import uk.gov.hmcts.opal.dto.legacy.common.CommentsAndNotes;
import uk.gov.hmcts.opal.dto.legacy.common.EnforcementOverride;
import uk.gov.hmcts.opal.dto.legacy.utils.ValidationUtils;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "response")
public class LegacyUpdateDefendantAccountResponse implements ToXmlString {

    @XmlElement(name = "defendant_account_id")
    @NotNull
    private String defendantAccountId;

    @XmlElement(name = "version")
    @NotNull
    private Integer version;

    @XmlElement(name = "comment_and_notes")
    private CommentsAndNotes commentAndNotes;

    @XmlElement(name = "enforcement_court_id")
    @Size(min = 1)
    private String enforcementCourtId;

    @XmlElement(name = "collection_order")
    private CollectionOrder collectionOrder;

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
