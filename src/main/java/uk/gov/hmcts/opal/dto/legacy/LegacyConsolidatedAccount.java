package uk.gov.hmcts.opal.dto.legacy;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.util.LocalDateAdapter;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class LegacyConsolidatedAccount {

    @JsonProperty("account_id")
    @XmlElement(name = "account_id")
    private Long accountId;

    @JsonProperty("account_number")
    @XmlElement(name = "account_number")
    private String accountNumber;

    @JsonProperty("first_name")
    @XmlElement(name = "first_name")
    private String firstName;

    @JsonProperty("last_name")
    @XmlElement(name = "last_name")
    private String lastName;

    @JsonProperty("date_imposed")
    @XmlElement(name = "date_imposed")
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate dateImposed;

    @JsonProperty("imposed_by")
    @XmlElement(name = "imposed_by")
    private String imposedBy;

    @JsonProperty("reference")
    @XmlElement(name = "reference")
    private String reference;
}
