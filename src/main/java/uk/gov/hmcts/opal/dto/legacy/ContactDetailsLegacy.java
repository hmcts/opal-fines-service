package uk.gov.hmcts.opal.dto.legacy;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class ContactDetailsLegacy {

    @XmlElement(name = "primary_email_address")
    private String primaryEmailAddress;

    @XmlElement(name = "secondary_email_address")
    private String secondaryEmailAddress;

    @XmlElement(name = "mobile_telephone_number")
    private String mobileTelephoneNumber;

    @XmlElement(name = "home_telephone_number")
    private String homeTelephoneNumber;

    @XmlElement(name = "work_telephone_number")
    private String workTelephoneNumber;
}
