package uk.gov.hmcts.opal.dto;


import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.opal.util.LocalDateAdapter;
import uk.gov.hmcts.opal.util.LocalDateTimeAdapter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@XmlRootElement(name = "party")
@XmlAccessorType(XmlAccessType.FIELD)
public class PartyDto implements ToJsonString {
    private Long partyId;
    private boolean organisation;
    private String organisationName;
    private String surname;
    private String forenames;
    private String title;
    private String addressLine1;
    private String addressLine2;
    private String addressLine3;
    private String addressLine4;
    private String addressLine5;
    private String postcode;
    private String accountType;
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate dateOfBirth;
    private Short age;
    private String niNumber;
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime lastChangedDate;
}
