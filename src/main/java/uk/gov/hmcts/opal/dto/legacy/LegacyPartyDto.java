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
import uk.gov.hmcts.opal.util.LocalDateAdapter;

import java.time.LocalDate;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonRootName(value = "party")
@XmlAccessorType(XmlAccessType.FIELD)
public class LegacyPartyDto {

    @JsonProperty("party_id")
    @XmlElement(name = "party_id")
    private Integer partyId;

    @JsonProperty("association_type")
    @XmlElement(name = "association_type")
    private String associationType;

    @JsonProperty("debtor")
    @XmlElement(name = "debtor")
    private Boolean debtor;

    @JsonProperty("organisation")
    @XmlElement(name = "organisation")
    private Boolean organisation;

    @JsonProperty("organisation_name")
    @XmlElement(name = "organisation_name")
    private String organisationName;

    @JsonProperty("surname")
    @XmlElement(name = "surname")
    private String surname;

    @JsonProperty("forenames")
    @XmlElement(name = "forenames")
    private String forenames;

    @JsonProperty("initials")
    @XmlElement(name = "initials")
    private String initials;

    @JsonProperty("title")
    @XmlElement(name = "title")
    private String title;

    @JsonProperty("birth_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    @XmlElement(name = "birth_date")
    private LocalDate birthDate;

    @JsonProperty("age")
    @XmlElement(name = "age")
    private Integer age;

    @JsonProperty("national_insurance_number")
    @XmlElement(name = "national_insurance_number")
    private String nationalInsuranceNumber;

    @JsonProperty("last_changed_date")
    @XmlElement(name = "last_changed_date")
    private String lastChangedDate;

    @JsonProperty("full_name")
    @XmlElement(name = "full_name")
    private String fullName;

    @JsonProperty("address_line_1")
    @XmlElement(name = "address_line_1")
    private String addressLine1;

    @JsonProperty("address_line_2")
    @XmlElement(name = "address_line_2")
    private String addressLine2;

    @JsonProperty("address_line_3")
    @XmlElement(name = "address_line_3")
    private String addressLine3;

    @JsonProperty("address_line_4")
    @XmlElement(name = "address_line_4")
    private String addressLine4;

    @JsonProperty("address_line_5")
    @XmlElement(name = "address_line_5")
    private String addressLine5;

    @JsonProperty("postcode")
    @XmlElement(name = "postcode")
    private String postcode;

}
