package uk.gov.hmcts.opal.dto.legacy.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.ToJsonString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@XmlAccessorType(XmlAccessType.FIELD)
public class CommentsAndNotes implements ToJsonString {

    @XmlElement(name = "account_comment")
    @JsonProperty("account_comment")
    private String accountComment;

    @XmlElement(name = "free_text_note_1")
    @JsonProperty("free_text_note_1")
    private String freeTextNote1;

    @XmlElement(name = "free_text_note_2")
    @JsonProperty("free_text_note_2")
    private String freeTextNote2;

    @XmlElement(name = "free_text_note_3")
    @JsonProperty("free_text_note_3")
    private String freeTextNote3;
}
