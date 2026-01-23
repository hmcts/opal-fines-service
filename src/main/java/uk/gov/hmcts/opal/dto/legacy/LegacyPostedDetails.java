package uk.gov.hmcts.opal.dto.legacy;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.util.LocalDateTimeAdapter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name = "posted_details")
@XmlAccessorType(XmlAccessType.FIELD)
public class LegacyPostedDetails {

    @XmlElement(name = "posted_date")
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime postedDate;

    @XmlElement(name = "posted_by")
    private String postedBy;

    @XmlElement(name = "posted_name")
    private String postedByName;
}
