package uk.gov.hmcts.opal.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.opal.util.LocalDateTimeAdapter;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@XmlRootElement(name = "note")
@XmlAccessorType(XmlAccessType.FIELD)
public class NoteDto implements ToJsonString {

    @JsonProperty("note_id")
    private Long noteId;

    @JsonProperty("note_type")
    private String noteType;

    @JsonProperty("associated_record_type")
    private String associatedRecordType;

    @JsonProperty("associated_record_id")
    private String associatedRecordId;

    @JsonProperty("business_unit_id")
    private Short businessUnitId;

    @JsonProperty("note_text")
    private String noteText;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    @JsonProperty("posted_date")
    private LocalDateTime postedDate;

    @JsonProperty("posted_by")
    @XmlElement(name = "postedBy")
    private String businessUnitUserId;

    @JsonProperty("posted_by_name")
    @XmlElement(name = "postedByUserId")
    private String postedByUsername;
}
