package uk.gov.hmcts.opal.dto.legacy;

import jakarta.xml.bind.annotation.XmlElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LegacyPostedDetails {

    @XmlElement(name = "posted_date")
    private LocalDate postedDate;

    @XmlElement(name = "posted_by")
    private String postedBy;

    @XmlElement(name = "posted_name")
    private String postedByName;
}
