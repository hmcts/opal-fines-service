package uk.gov.hmcts.opal.dto.legacy;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.util.LocalDateTimeAdapter;

import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonRootName(value = "account_activity")
@XmlAccessorType(XmlAccessType.FIELD)
public class AccountActivityDto {

    @JsonProperty("activity_id")
    @XmlElement(name = "activity_id")
    private Integer activityId;

    @JsonProperty("activity_type")
    @XmlElement(name = "activity_type")
    private String activityType;

    @JsonProperty("activity_type_code")
    @XmlElement(name = "activity_type_code")
    private String activityTypeCode;

    @JsonProperty("activity_text")
    @XmlElement(name = "activity_text")
    private String activityText;

    @JsonProperty("posted_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    @XmlElement(name = "posted_date")
    private LocalDateTime postedDate;

    @JsonProperty("posted_by")
    @XmlElement(name = "posted_by")
    private String postedBy;

    @JsonProperty("amount")
    @XmlElement(name = "amount")
    private Double amount;
}
